package io.defitrack.ens

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EnsRouterConfig {

    @Bean
    fun ensRoutes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        routeBuilder.route("ens") {
            it.path(true, "/ens/**")
                .filters { filter ->
                    filter.rewritePath(
                        "/ens/(?<segment>.*)",
                        "/\${segment}"
                    )
                }
                .uri("http://defitrack-ens.default.svc.cluster.local:8080")
        }.build()
        return routeBuilder.build()
    }
}