package io.defitrack.statistics

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StatisticsRouterConfig {

    @Bean
    fun statisticsRoutes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        routeBuilder.route("statistics") {
            it.path(true, "/statistics/**")
                .filters { filter ->
                    filter.rewritePath(
                        "/statistics/(?<segment>.*)",
                        "/\${segment}"
                    )
                }
                .uri("http://defitrack-statitics:8080")
        }.build()
        return routeBuilder.build()
    }
}