package com.karthik.users.model

import java.util.*

data class User(val firstName: String,
				val lastName: String,
				val id: String = UUID.randomUUID().toString())