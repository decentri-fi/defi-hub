package io.defitrack.protocol

import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class ProtocolHandler {

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