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
    fun protocolRoutes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        Protocol.values().forEach { proto ->
            routeBuilder.route(proto.name) {
                it.path(true, "/${proto.slug}/**")
                    .filters { filter ->
                        filter.rewritePath(
                            "/${proto.slug}/(?<segment>.*)",
                            "/\${segment}"
                        )
                    }
                    .uri("http://defitrack-${proto.slug}:8080/api")
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