package com.karthik.users.persistence

import com.karthik.users.model.User
import com.karthik.users.persistence.AttributeValueBuilders.string
import com.karthik.users.persistence.DynamoRequestBuilders.getRequest
import com.karthik.users.persistence.DynamoRequestBuilders.putRequest
import com.karthik.users.persistence.TableNames.users
import com.karthik.users.persistence.UserMapper.fromDynamoItem
import com.karthik.users.persistence.UserMapper.toDynamoItem
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
import java.util.stream.Collectors.toList

@Repository
class UserRepositoryImpl(val dbClient: DBClient) : UserRepository {

    override fun addUser(user: User): Mono<User> {

        val putItemRequest = putRequest(users, toDynamoItem(user))
        val getItemRequest = getRequest(users, mapOf(ID to string(user.id)))

        return Mono.fromFuture(
                dbClient.getClient()
                        .putItem(putItemRequest)
                        .thenComposeAsync { retrieveUser(getItemRequest) }
        )
    }

    private fun retrieveUser(getItemRequest: GetItemRequest) =
            dbClient.getClient()
                    .getItem(getItemRequest)
                    .thenApplyAsync { fromDynamoItem(it.item()) }


    override fun findAllUsers(): Flux<User> {
        return Mono.fromFuture { scanForAllUsers().thenApplyAsync { fromScanResponseToList(it) } }
                   .flatMapIterable { it }
    }

    private fun scanForAllUsers(maxResults: Int = 10) =
            dbClient.getClient()
                    .scan(
                        ScanRequest
                            .builder()
                            .tableName(users)
                            .limit(maxResults)
                            .build()
                    )

    private fun fromScanResponseToList(response: ScanResponse) =
            response.items()
                    .stream()
                    .map { fromDynamoItem(it) }
                    .collect(toList())

    override fun findUserById(userId: String): Mono<User> {
        val getItemRequest = getRequest(users, mapOf(ID to string(userId)))

        return Mono.fromCompletionStage(
                retrieveUser(getItemRequest)
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
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey))
                )
                .build()
    }
}
