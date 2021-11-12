package io.defitrack.price

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PriceRouterConfig {

    @Bean
    fun priceRoutes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        routeBuilder.route("price") {
            it.path(true, "/price")
                .filters { filter ->
                    filter.rewritePath(
                        "/price(?<segment>.*)",
                        "/\${segment}"
                    )
                }
                .uri("http://defitrack-price:8080")
        }
        return routeBuilder.build()
    }
}