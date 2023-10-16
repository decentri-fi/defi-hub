package io.defitrack.protocol

import io.defitrack.protocol.distribution.ProtocolDistributionConfig
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse


@Configuration
class ProtocolRouterConfig(
    private val protocolDistributionConfig: ProtocolDistributionConfig
) {

    @Bean
    fun company2Routes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        protocolDistributionConfig.getConfigs().forEach {node ->
            node.companies.forEach { company ->
                routeBuilder.route("companies-" + company.slug) {
                    it.path(true, "/${company.slug}/**")
                        .filters { filter ->
                            filter.rewritePath(
                                "/${company.slug}/(?<segment>.*)",
                                "/${company.slug}/\${segment}"
                            )
                        }
                        .uri("http://defitrack-group-${node.name}.default.svc.cluster.local:8080/api")
                }
            }
        }
        return routeBuilder.build()
    }

    @Bean
    fun protocolListRoute(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()
        routeBuilder.route("protocols") {
            it.path(true, "/protocols")
                .uri("http://defitrack-meta.default.svc.cluster.local:8080/protocols")
        }.build()
        return routeBuilder.build()
    }
}