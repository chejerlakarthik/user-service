package com.karthik.users.beans

import com.karthik.users.handler.UserHandler
import com.karthik.users.persistence.DBClient
import com.karthik.users.persistence.UserRepositoryImpl
import com.karthik.users.routes.RoutesConfig
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans

/**
 * This class is not being used at the moment
 */
class BeansConfig: ApplicationContextInitializer<GenericApplicationContext> {

    override fun initialize(context: GenericApplicationContext) = beans {
        bean { DBClient() }
        bean { UserRepositoryImpl(ref()) }
        bean { UserHandler(ref()) }
        bean { RoutesConfig(ref()) }
    }.initialize(context)

}