package io.defitrack.network

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse


@Configuration
class NetworkRouterConfig {

    @Bean
    fun networksRoute(networkHandler: NetworkHandler): RouterFunction<ServerResponse> {
        return RouterFunctions
            .route(GET("/networks"), networkHandler::getNetworks)
    }
}