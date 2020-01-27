package com.karthik.users.persistence

import com.karthik.users.model.User
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserRepository {

    fun findAllUsers(): Flux<User>

    fun findUserById(userId: String): Mono<User>

    fun addUser(user: User): Mono<User>

}
