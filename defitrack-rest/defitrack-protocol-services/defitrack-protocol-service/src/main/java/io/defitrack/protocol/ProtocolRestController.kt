package io.defitrack.protocol

import io.defitrack.market.farming.DefaultFarmingMarketRestController
import io.defitrack.market.lending.DefaultLendingMarketsRestController
import io.defitrack.market.pooling.DefaultPoolingMarketRestController
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
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
        return protocolProvider.getProtocol().toVO()
    }
}

fun Protocol.toVO(): ProtocolVO {
    return with(
        ProtocolVO(
            name = this.name,
            logo = this.getImage(),
            slug = this.slug,
            primitives = this.primitives,
            website = this.website
        )
    ) {
        if (this.primitives.contains(DefiPrimitive.FARMING)) {
            add(
                WebMvcLinkBuilder.linkTo(DefaultFarmingMarketRestController::class.java).slash("all-markets")
                    .withRel("farming")
            )
        }
        if (this.primitives.contains(DefiPrimitive.POOLING)) {
            add(
                WebMvcLinkBuilder.linkTo(DefaultPoolingMarketRestController::class.java).slash("all-markets").withRel("pooling")
            )
        }
        if (this.primitives.contains(DefiPrimitive.LENDING)) {
            add(
                WebMvcLinkBuilder.linkTo(DefaultLendingMarketsRestController::class.java)
                    .slash("all-markets")
                    .withRel("lending")
            )
        }
        this
    }
}