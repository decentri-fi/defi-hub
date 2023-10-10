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

        Protocol.entries.forEach { protocol ->
            routeBuilder.route(protocol.name) {
                it.path(true, "/${protocol.slug}/**")
                    .filters { filter ->
                        filter.rewritePath(
                            "/${protocol.slug}/(?<segment>.*)",
                            "/${protocol.slug}/\${segment}"
                        )
                    }
                    .uri("http://defitrack-${protocol.company.slug}.default.svc.cluster.local:8080/api")
            }
        }
        return routeBuilder.build()
    }

    @Bean
    fun protocolListRoute(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()
        routeBuilder.route("protocols") {
            it.path(true, "/protocols")
                .uri("http://defitrack-meta.default.svc.cluster.local:8080/protocols")
        }.build()
        return routeBuilder.build()
    }
}