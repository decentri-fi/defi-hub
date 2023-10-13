package io.defitrack.meta

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetaRouterConfig {

    @Bean
    fun metaRoutes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        routeBuilder.route("meta") {
            it.path(true, "/meta/**")
                .filters { filter ->
                    filter.rewritePath(
                        "/meta/(?<segment>.*)",
                        "/\${segment}"
                    )
                }
                .uri("http://defitrack-meta.default.svc.cluster.local:8080")
        }.build()
        return routeBuilder.build()
    }
}