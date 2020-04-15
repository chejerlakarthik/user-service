package com.karthik.users.handler

import com.karthik.users.model.User
import com.karthik.users.persistence.UserRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class UserHandler(private val userRepository: UserRepository) {

    fun addNewUser(request: ServerRequest): Mono<ServerResponse> {
        return extractEntity(request, User::class.java)
                    .flatMap { userRepository.addUser(it) }
                    .flatMap { ServerResponse.ok().bodyValue(it) }
    }

    fun getAllUsers(request: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().body(userRepository.findAllUsers())
    }

    fun getUserById(request: ServerRequest): Mono<ServerResponse> {
        val userId = request.pathVariable("id")
        return ServerResponse.ok().body(userRepository.findUserById(userId))
    }

    private fun extractEntity(request: ServerRequest, clazz: Class<User>) = request.bodyToMono(clazz)

}