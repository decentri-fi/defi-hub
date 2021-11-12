package io.defitrack.erc20

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class ERC20RouterConfig {

    @Bean
    fun erc20Routes(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes().route("erc20") {
            it.path(true, "/erc20/**")
                .filters { filter ->
                    filter.rewritePath(
                        "/erc20/(?<segment>.*)",
                        "/\${segment}"
                    )
                }
                .uri("http://defitrack-erc20:8080")
        }.build()
    }
}