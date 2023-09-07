package io.defitrack.abi

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ABIRouterConfig {

    @Bean
    fun abiRoutes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        routeBuilder.route("abi") {
            it.path(true, "/abi")
                .filters { filter ->
                    filter.rewritePath(
                        "/abi(?<segment>.*)",
                        "/\${segment}"
                    )
                }
                .uri("http://defitrack-abi.default.svc.cluster.local:8080")
        }
        return routeBuilder.build()
    }
}