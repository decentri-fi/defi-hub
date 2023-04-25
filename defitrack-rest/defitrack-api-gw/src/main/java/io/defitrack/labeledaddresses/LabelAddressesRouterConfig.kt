package io.defitrack.labeledaddresses

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LabelAddressesRouterConfig {

    @Bean
    fun labeledAddressesRoutes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        routeBuilder.route("labeledaddresse") {
            it.path(true, "/labeled-addresses")
                .filters { filter ->
                    filter.rewritePath(
                        "/labeled-addresses(?<segment>.*)",
                        "/\${segment}"
                    )
                }
                .uri("http://labeled-addresses:8080")
        }.route("labeladdresses") {
            it.path(true, "/labeled-addresses/**")
                .filters { filter ->
                    filter.rewritePath(
                        "/labeled-addresses/(?<segment>.*)",
                        "/\${segment}"
                    )
                }
                .uri("http://labeled-addresses:8080")
        }.build()
        return routeBuilder.build()
    }
}