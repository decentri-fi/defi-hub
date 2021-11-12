package io.codechef.defitrack.claimable

import io.codechef.defitrack.network.toVO
import io.codechef.defitrack.protocol.toVO
import io.defitrack.abi.PriceResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class DefaultClaimableRestController(
    private val claimableServices: List<ClaimableService>,
    private val priceService: PriceResource
) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping(value = ["/{address}/claimables"])
    fun claimables(
        @PathVariable("address") address: String,
    ): List<ClaimableElementVO> {
        return try {
            claimableServices.flatMap {
                it.claimables(address)
            }.map {
                ClaimableElementVO(
                    name = it.name,
                    address = it.address,
                    type = it.type,
                    protocol = it.protocol.toVO(),
                    network = it.network.toVO(),
                    claimableToken = ClaimableTokenVO(
                        it.claimableToken.name,
                        it.claimableToken.symbol,
                        it.claimableToken.amount,
                        it.claimableToken.amount?.let { amount ->
                            priceService.calculatePrice(it.claimableToken.symbol, amount)
                        } ?: .0
                    )
                )
            }
        } catch (ex: Exception) {
            logger.error("Unable to fetch claimables: ${ex.message}")
            emptyList()
        }
    }
}