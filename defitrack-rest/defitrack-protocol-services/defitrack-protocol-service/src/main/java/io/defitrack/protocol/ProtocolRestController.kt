package io.defitrack.protocol

import io.defitrack.market.farming.DefaultFarmingMarketRestController
import io.defitrack.market.lending.DefaultLendingMarketsRestController
import io.defitrack.market.pooling.DefaultPoolingMarketRestController
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
            if(protocol.primitives.contains(DefiPrimitive.FARMING)) {
                add(linkTo(DefaultFarmingMarketRestController::class.java).withRel("farming"))
            }
            if(protocol.primitives.contains(DefiPrimitive.POOLING)) {
                add(linkTo(DefaultPoolingMarketRestController::class.java).withRel("pooling"))
            }
            if(protocol.primitives.contains(DefiPrimitive.LENDING)) {
                add(linkTo(DefaultLendingMarketsRestController::class.java).withRel("lending"))
            }
            this
        }
    }
}