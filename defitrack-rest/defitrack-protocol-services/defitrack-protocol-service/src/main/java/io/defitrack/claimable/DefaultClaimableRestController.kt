package io.defitrack.claimable

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.toVO
import kotlinx.coroutines.async
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
    private val claimableRewardProviders: List<ClaimableRewardProvider>,
    private val priceResource: PriceResource,
    private val defaultClaimableRewardProvider: DefaultClaimableRewardProvider
) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping(value = ["/{address}/claimables"])
    fun claimables(
        @PathVariable("address") address: String,
    ): List<ClaimableVO> = runBlocking {
        val fromProviders = async {
            getFromProviders(address).map { toVO(it) }
        }

        val fromDefaultProvider = async {
            getFromDefaultProvider(address).map { toVO(it) }
        }

        (fromProviders.await() + fromDefaultProvider.await()).filterNotNull()
    }

    private suspend fun toVO(it: Claimable) = try {
        val amount = it.amount.asEth(it.claimableTokens.firstOrNull()?.decimals ?: 18)
        val claimableInDollar = priceResource.calculatePrice(
            PriceRequest(
                address = it.claimableTokens.first().address,
                network = it.network,
                amount = amount,
                type = it.claimableTokens.first().type
            )
        )

        ClaimableVO(
            id = it.id,
            name = it.name,
            type = it.type,
            protocol = it.protocol.toVO(),
            network = it.network.toVO(),
            token = it.claimableTokens.first(),
            amount = it.amount.asEth(it.claimableTokens.first().decimals).toDouble(),
            dollarValue = claimableInDollar,
            claimTransaction = it.claimTransaction
        )
    } catch (ex: Exception) {
        logger.error("Error while fetching claimable", ex)
        null
    }

    private suspend fun getFromDefaultProvider(user: String): List<Claimable> {
        return defaultClaimableRewardProvider.claimables(
            user
        )
    }

    private suspend fun getFromProviders(address: String) = claimableRewardProviders.flatMap {
        it.claimables(address)
    }
}