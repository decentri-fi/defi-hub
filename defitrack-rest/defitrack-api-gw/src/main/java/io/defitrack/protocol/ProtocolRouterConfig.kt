package io.defitrack.protocol

import io.defitrack.protocol.distribution.ProtocolDistributionConfig
import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun company2Routes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        protocolDistributionConfig.getConfigs().forEach {node ->
            node.companies.forEach { company ->
                try {
                   company.fetchProtocols().forEach { protocol ->
                       routeBuilder.route("protocol-" + protocol.slug) {
                           it.path(true, "/${protocol.slug}/**")
                               .filters { filter ->
                                   filter.rewritePath(
                                       "/${protocol.slug}/(?<segment>.*)",
                                       "/${protocol.slug}/\${segment}"
                                   )
                               }
                               .uri("http://defitrack-group-${node.name}.default.svc.cluster.local:8080/api")
                       }
                   }
                } catch (ex: Exception) {
                    logger.info("unable to write route for company $company")
                    ex.printStackTrace()
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