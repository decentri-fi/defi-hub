package io.defitrack.protocol

import io.defitrack.market.farming.DefaultFarmingMarketRestController
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class ProtocolRestController(
    private val protocolProvider: ProtocolProvider
) {

    @GetMapping
    fun getProtocol(): ProtocolVO {
        val protocol = protocolProvider.getProtocol().toVO()
        return with(protocol) {
            val farming = linkTo(DefaultFarmingMarketRestController::class.java).slash("all-markets").withRel("farming")
            add(farming)
            this
        }
    }
}