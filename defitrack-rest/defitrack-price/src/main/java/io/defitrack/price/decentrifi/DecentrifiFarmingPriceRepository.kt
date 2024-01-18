package io.defitrack.price.decentrifi

import arrow.fx.coroutines.parMap
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.token.FungibleToken
import io.defitrack.market.farming.vo.FarmingMarketVO
import io.defitrack.network.NetworkInformation
import io.defitrack.price.external.ExternalPrice
import io.defitrack.protocol.ProtocolInformation
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiFarmingPriceRepository(
    private val httpClient: HttpClient
) {

    val logger = LoggerFactory.getLogger(this::class.java)
    val cache = Cache.Builder<String, ExternalPrice>().build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 6)
    fun populateFarmPrices() = runBlocking {
        logger.info("fetching prices from decentrifi farms")
        val protocols = getProtocols()
        protocols.map { proto ->
            try {
                getFarms(proto)
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

    fun contains(token: FungibleToken): Boolean {
        return cache.get(toIndex(token.network, token.address)) != null
    }

    private fun toIndex(network: NetworkInformation, address: String) =
        "${network.name}-${address.lowercase()}"

    suspend fun getProtocols(): List<ProtocolInformation> {
        return httpClient.get("https://api.decentri.fi/protocols").body()
    }

    suspend fun getFarms(protocol: ProtocolInformation): List<FarmingMarketVO> {
        val result =
            httpClient.get("https://api.decentri.fi/${protocol.slug}/farming/all-markets")
        return if (result.status.isSuccess())
            result.body()
        else {
            logger.error("Unable to fetch farms for ${protocol.name} ${result.bodyAsText()}")
            emptyList()
        }
    }

    suspend fun getPrice(token: FungibleToken): BigDecimal {
        return cache.get(toIndex(token.network, token.address))?.price ?: BigDecimal.ZERO
    }
}
