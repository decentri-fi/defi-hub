package io.defitrack.price.decentrifi

import arrow.fx.coroutines.parMap
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.marketinfo.port.out.Markets
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.price.external.ExternalPrice
import io.defitrack.protocol.port.`in`.ProtocolResource
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnProperty("oracles.farming_markets.enabled", havingValue = "true", matchIfMissing = true)
class DecentrifiFarmingPriceRepository(
    private val marketResource: Markets,
    private val protocolResource: ProtocolResource
) {

    val logger = LoggerFactory.getLogger(this::class.java)
    val cache = Cache.Builder<String, ExternalPrice>().build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 6)
    fun populateFarmPrices() = runBlocking {
        logger.info("fetching prices from decentrifi farms")
        protocolResource.getProtocols().map { proto ->
            try {
                marketResource.getFarmingMarkets(proto.slug)
                    .filter {
                        it.token != null && (it.marketSize ?: BigDecimal.ZERO) > BigDecimal.ONE
                    }.parMap(concurrency = 12) { farm ->

                        val pricePerToken = (farm.marketSize ?: BigDecimal.ZERO).dividePrecisely(
                            farm.token!!.totalSupply.asEth(farm.token!!.decimals)
                        )

                        toIndex(farm.network, farm.token!!.address) to ExternalPrice(
                            farm.token!!.address,
                            farm.network.toNetwork(),
                            pricePerToken,
                            "decentrifi-farming"
                        )
                    }.forEach { farm ->
                        cache.put(
                            farm.first, farm.second
                        )
                    }
            } catch (ex: Exception) {
                logger.error("Unable to import price for proto ${proto.slug}", ex)
            }
        }

        logger.info("Decentrifi Farming Price Repository populated with ${cache.asMap().entries.size} prices")
    }

    fun contains(token: FungibleTokenInformation): Boolean {
        return cache.get(toIndex(token.network, token.address)) != null
    }

    private fun toIndex(network: NetworkInformation, address: String) =
        "${network.name}-${address.lowercase()}"

    suspend fun getPrice(token: FungibleTokenInformation): BigDecimal {
        return cache.get(toIndex(token.network, token.address))?.price ?: BigDecimal.ZERO
    }
}
