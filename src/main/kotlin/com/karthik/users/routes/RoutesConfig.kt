package com.karthik.users.routes

import com.karthik.users.handler.UserHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.router

@Configuration
class RoutesConfig(val userHandler: UserHandler) {

    @Bean
    fun routes() = router {
        ("/users" and accept(APPLICATION_JSON)).nest {
            POST("/", userHandler::addNewUser)
            GET("/", userHandler::getAllUsers)
            GET("/{id}", userHandler::getUserById)
        }

    }
}