package io.defitrack.protocol

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse


@Configuration
class CompanyRouterConfig {

    fun getCompanies(serverRequest: ServerRequest) = ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                Company.entries.map {
                    CompanyVO(
                        name = it.prettyName,
                        slug = it.slug,
                    )
                }
            )
        )

    @Bean
    fun companiesRoute(): RouterFunction<ServerResponse> {
        return RouterFunctions
            .route(GET("/companies"), ::getCompanies)
    }
}