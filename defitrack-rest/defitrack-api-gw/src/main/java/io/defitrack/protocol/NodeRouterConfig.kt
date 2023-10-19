package io.defitrack.protocol

import io.defitrack.protocol.distribution.Node
import io.defitrack.protocol.distribution.ProtocolDistributionConfig
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import kotlin.time.Duration.Companion.hours


@Configuration
class NodeRouterConfig(
    private val distributionConfig: ProtocolDistributionConfig
) {

    val cache = Cache.Builder<String, List<Node>>().expireAfterWrite(6.hours).build()

    fun getNodes(serverRequest: ServerRequest): Mono<ServerResponse> = runBlocking {
        ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromValue(
                    fetchNodes()
                )
            )
    }

    private suspend fun fetchNodes() = cache.get("nodes") {
        distributionConfig.getConfigs()
    }

    @Bean
    fun nodesRoute(): RouterFunction<ServerResponse> {
        return RouterFunctions
            .route(GET("/nodes"), ::getNodes)
    }

    @Bean
    fun nodesSubRoutes(builder: RouteLocatorBuilder): RouteLocator = runBlocking {
        val routeBuilder = builder.routes()

        fetchNodes().forEach { node ->
            routeBuilder.route("node-" + node.name) {
                it.path(true, "/nodes/${node.name}/**")
                    .filters { filter ->
                        filter.rewritePath(
                            "/nodes/${node.name}/(?<segment>.*)",
                            "/\${segment}"
                        )
                    }
                    .uri("http://defitrack-group-${node.name}.default.svc.cluster.local:8080/api")
            }
        }
        routeBuilder.build()
    }
}