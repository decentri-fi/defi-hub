package io.defitrack.events

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventsRouterConfig {

    @Bean
    fun eventsRoutes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        routeBuilder.route("events") {
            it.path(true, "/events/**")
                .filters { filter ->
                    filter.rewritePath(
                        "/events/(?<segment>.*)",
                        "/\${segment}"
                    )
                }
                .uri("http://defitrack-events:8080")
        }.build()
        return routeBuilder.build()
    }
}