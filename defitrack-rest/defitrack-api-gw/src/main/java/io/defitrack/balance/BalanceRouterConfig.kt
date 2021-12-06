package io.defitrack.balance

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BalanceRouterConfig {

    @Bean
    fun balanceRoutes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        routeBuilder.route("balance") {
            it.path(true, "/balance")
                .filters { filter ->
                    filter.rewritePath(
                        "/balance(?<segment>.*)",
                        "/\${segment}"
                    )
                }
                .uri("http://defitrack-balance:8080")
        }
        return routeBuilder.build()
    }
}