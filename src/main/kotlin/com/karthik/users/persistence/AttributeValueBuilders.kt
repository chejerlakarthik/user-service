package com.karthik.users.persistence

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

object AttributeValueBuilders {

    fun string(value: String): AttributeValue {
        return AttributeValue.builder().s(value).build()
    }

    fun boolean(value: Boolean): AttributeValue {
        return AttributeValue.builder().bool(value).build()
    }

    fun number(value: String): AttributeValue {
        return AttributeValue.builder().n(value).build()
    }

}
