package io.defitrack.protocol

import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class CompanyHandler {

    fun getCompanies(serverRequest: ServerRequest) = ServerResponse.ok()
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                Company.values().map {
                    CompanyVO(
                        name = it.prettyName,
                        slug = it.slug,
                    )
                }
            )
        )
}