package io.defitrack.protocol

import io.defitrack.protocol.distribution.Node
import io.defitrack.protocol.distribution.ProtocolDistributionConfig
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
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
                    cache.get("nodes") {
                        distributionConfig.getConfigs()
                    }
                )
            )
    }

    @Bean
    fun nodesRoute(): RouterFunction<ServerResponse> {
        return RouterFunctions
            .route(GET("/nodes"), ::getNodes)
    }
}