package io.defitrack.network

import io.defitrack.common.network.Network
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration
class NetworkRouterConfig {

    @Bean
    fun networkListRoute(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()
        routeBuilder.route("networks") {
            it.path(true, "/networks")
                .uri("http://defitrack-meta.default.svc.cluster.local:8080/networks")
        }.build()
        return routeBuilder.build()
    }

    @Bean
    fun networkRoutes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        Network.entries.forEach { network ->
            routeBuilder.route(network.name) {
                it.path(true, "/networks/${network.slug}/**")
                    .filters { filter ->
                        filter.rewritePath(
                            "/networks/${network.slug}/(?<segment>.*)",
                            "/\${segment}"
                        )
                    }
                    .uri("http://defitrack-${network.slug}.default.svc.cluster.local:8080/api")
            }
        }
        return routeBuilder.build()
    }
}