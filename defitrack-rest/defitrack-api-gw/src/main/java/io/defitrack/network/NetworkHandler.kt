package io.defitrack.network

import io.defitrack.common.network.Network
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class NetworkHandler {

    fun getNetworks(serverRequest: ServerRequest) = ServerResponse.ok()
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                Network.values().filter(Network::hasMicroService).map(Network::toVO)
            )
        )
}