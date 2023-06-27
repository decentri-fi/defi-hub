package io.defitrack.protocol

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse


@Configuration
class ProtocolRouterConfig {

    @Bean
    fun companyRoutes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        Protocol.values().forEach { protocol ->
            routeBuilder.route(protocol.name) {
                it.path(true, "/${protocol.slug}/**")
                    .filters { filter ->
                        filter.rewritePath(
                            "/${protocol.company.slug}/${protocol.slug}/(?<segment>.*)",
                            "/${protocol.slug}/\${segment}"
                        )
                    }
                    .uri("http://defitrack-${protocol.company.slug}:8080/api")
            }
        }
        return routeBuilder.build()
    }


    @Bean
    fun protocolsRoute(protocolHandler: ProtocolHandler): RouterFunction<ServerResponse> {
        return RouterFunctions
            .route(GET("/protocols"), protocolHandler::getProtocols)
    }
}