package com.karthik.users.controller

import com.karthik.users.model.User
import com.karthik.users.persistence.UserRepository
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class UserController(val userRepository: UserRepository) {

    @GetMapping("/users")
    fun getAllUsers(): Flux<User> {
        return userRepository.findAllUsers()
    }

    @GetMapping("/users/{userId}")
    fun getUserById(@PathVariable userId: String): Mono<User> {
        return userRepository.findUserById(userId)
    }

    @PostMapping("/users")
    fun addUser(@RequestBody user: User): Mono<User> {
        return userRepository.addUser(user)
    }
}