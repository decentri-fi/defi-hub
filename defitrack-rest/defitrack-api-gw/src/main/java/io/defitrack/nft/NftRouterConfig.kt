package io.defitrack.ens

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NftRouterConfig {

    @Bean
    fun nftRoutes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        routeBuilder.route("nft") {
            it.path(true, "/nft/**")
                .filters { filter ->
                    filter.rewritePath(
                        "/nft/(?<segment>.*)",
                        "/\${segment}"
                    )
                }
                .uri("http://defitrack-nft:8080")
        }.build()
        return routeBuilder.build()
    }
}