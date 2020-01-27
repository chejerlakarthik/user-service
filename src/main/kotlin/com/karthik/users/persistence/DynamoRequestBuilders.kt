package com.karthik.users.persistence

import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest

object DynamoRequestBuilders {

    fun putRequest(tableName: String, valueMap: Map<String, AttributeValue>): PutItemRequest {
        return PutItemRequest
                .builder()
                .tableName(tableName)
                .item(valueMap)
                .build()
    }

    fun getRequest(tableName: String, lookUpKey: Map<String, AttributeValue>): GetItemRequest {
        return GetItemRequest
                .builder()
                .tableName(tableName)
                .key(lookUpKey)
                .build()
    }

}
