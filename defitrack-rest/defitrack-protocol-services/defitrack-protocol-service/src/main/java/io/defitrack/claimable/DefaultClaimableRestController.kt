package io.defitrack.claimable

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.toVO
import kotlinx.coroutines.runBlocking
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
    private val priceResource: PriceResource
) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping(value = ["/{address}/claimables"])
    fun claimables(
        @PathVariable("address") address: String,
    ): List<ClaimableVO> = runBlocking {
        try {
            claimableServices.flatMap {
                it.claimables(address)
            }.map {
                val amount = it.amount.asEth(it.claimableToken.decimals)
                val claimableInDollar = priceResource.calculatePrice(
                    PriceRequest(
                        address = it.claimableToken.address,
                        network = it.network,
                        amount = amount,
                        type = it.claimableToken.type
                    )
                )

                ClaimableVO(
                    id = it.id,
                    name = it.name,
                    address = it.address,
                    type = it.type,
                    protocol = it.protocol.toVO(),
                    network = it.network.toVO(),
                    token = it.claimableToken,
                    amount = it.amount.asEth(it.claimableToken.decimals).toDouble(),
                    dollarValue = claimableInDollar,
                    claimTransaction = it.claimTransaction
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            logger.error("Unable to fetch claimables: ${ex.message}")
            emptyList()
        }
    }
}