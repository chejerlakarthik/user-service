package com.karthik.users.persistence

import com.karthik.users.model.User
import com.karthik.users.persistence.AttributeValueBuilders.string
import com.karthik.users.persistence.DynamoRequestBuilders.getRequest
import com.karthik.users.persistence.DynamoRequestBuilders.putRequest
import com.karthik.users.persistence.TableNames.users
import com.karthik.users.persistence.UserTableFields.FIRST_NAME
import com.karthik.users.persistence.UserTableFields.ID
import com.karthik.users.persistence.UserTableFields.LAST_NAME
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.ScanResponse
import java.util.stream.Collectors

@Repository
class UserRepositoryImpl(val dbClient: DBClient) : UserRepository {

    override fun addUser(user: User): Mono<User> {

        val putItemRequest = putRequest(users, UserMapper.toDynamoItem(user))
        val getItemRequest = getRequest(users, mapOf(ID to string(user.id)))

        return Mono.fromCompletionStage(
                dbClient.getClient()
                        .putItem(putItemRequest)
                        .thenComposeAsync { retrieveUser(getItemRequest) }
        )
    }

    private fun retrieveUser(getItemRequest: GetItemRequest) =
            dbClient.getClient()
                    .getItem(getItemRequest)
                    .thenApplyAsync { UserMapper.fromDynamoItem(it.item()) }

    override fun findAllUsers(): Flux<User> {

        return Mono.fromFuture { scanForAllUsers().thenApplyAsync { fromScanResponseToList(it) } }
                   .flatMapIterable { it }
    }

    private fun scanForAllUsers() =
            dbClient.getClient().scan(ScanRequest.builder().tableName(TableNames.users).limit(10).build())

    private fun fromScanResponseToList(response: ScanResponse) =
            response.items().stream().map { x -> UserMapper.fromDynamoItem(x) }.collect(Collectors.toList())

    override fun findUserById(userId: String): Mono<User> {
        val getItemRequest = getRequest(users, mapOf(ID to string(userId)))

        return Mono.fromCompletionStage(
                dbClient.getClient()
                        .getItem(getItemRequest)
                        .thenApplyAsync { UserMapper.fromDynamoItem(it.item()) }
        )
    }

}

object UserMapper {

    fun toDynamoItem(user: User): Map<String, AttributeValue> {
        return mapOf(
                ID to string(user.id),
                FIRST_NAME to string(user.firstName),
                LAST_NAME to string(user.lastName)
        )
    }

    fun fromDynamoItem(record: Map<String, AttributeValue>): User {
        return User(
                firstName = record[FIRST_NAME]?.s()!!, // TODO: Refactor this ugly code
                lastName = record[LAST_NAME]?.s()!!,   // TODO: Refactor this ugly code
                id = record[ID]?.s()!!                 // TODO: Refactor this ugly code
        )
    }

}

object UserTableFields {
    const val ID = "id"
    const val FIRST_NAME = "firstName"
    const val LAST_NAME = "lastName"
}

@Component
class DBClient {

    @Value("\${aws.dynamodb.accessKeyId}")
    val accessKeyId: String = ""

    @Value("\${aws.dynamodb.secretAccessKey}")
    val secretAccessKey: String = ""

    fun getClient(): DynamoDbAsyncClient {
        return DynamoDbAsyncClient.builder()
                .region(Region.AP_SOUTHEAST_2)
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey))
                )
                .build()
    }
}
