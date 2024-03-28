package io.defitrack.price.external.adapter.decentrifi

import io.defitrack.adapter.output.domain.market.PoolingMarketInformationDTO
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.port.output.MarketClient
import io.defitrack.price.external.adapter.stable.StablecoinPriceProvider
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.out.ExternalPriceService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger

@Component
@ConditionalOnProperty("oracles.alienbase.enabled", havingValue = "true", matchIfMissing = true)
class DecentrifiAlienbaseV2PriceService(
    private val markets: MarketClient,
    private val stablecoinPriceProvider: StablecoinPriceProvider
) : ExternalPriceService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val prices = mutableListOf<ExternalPrice>()

    val weth = "0x4200000000000000000000000000000000000006"

    override suspend fun getAllPrices(): Flow<ExternalPrice> = channelFlow {
        val pools = getAlienbasePools()
        importUsdPairs(pools)
        //  importEthPairs(pools)
        logger.info("Decentri Alienbase V2 Underlying Price Repository populated with ${prices.size} prices")
        prices.forEach {
            send(it)
        }
    }

    private suspend fun importEthPairs(pools: List<PoolingMarketInformationDTO>) {

        val ethPrice = prices.find {
            it.address.lowercase() == weth
        }

        if (ethPrice == null) {
            logger.error("WETH price not found")
            return
        } else {
            logger.info("WETH price found, was ${ethPrice.price}")
        }

        val ethPairs = pools.filter { pool ->
            pool.breakdown?.any { share ->
                share.token.address.lowercase() == weth
            } ?: false
        }

        ethPairs.forEach { pool ->
            val ethPair = pool.breakdown?.find { share ->
                share.token.address.lowercase() == weth
            }

            val otherShare = pool.breakdown?.find { share ->
                share.token.address.lowercase() != weth
            }

            if (ethPair != null && otherShare != null && ethPair.reserveDecimal > BigDecimal.valueOf(5)) {
                if (otherShare.reserve > BigInteger.ZERO) {
                    val otherprice =
                        ethPair.reserve.asEth(ethPair.token.decimals).times(ethPrice.price).dividePrecisely(
                            otherShare.reserve.asEth(otherShare.token.decimals)
                        )
                    prices.add(
                        ExternalPrice(
                            otherShare.token.address,
                            pool.network.toNetwork(),
                            otherprice,
                            "decentrifi-alienbase",
                            pool.name,
                            importOrder()
                        )
                    )
                }
            }
        }
    }


    private suspend fun importUsdPairs(pools: List<PoolingMarketInformationDTO>) {
        val usdPairs = pools.filter { pool ->
            pool.breakdown?.any { share ->
                stablecoinPriceProvider.isStable(pool.network.toNetwork()).invoke(share.token.address)
            } ?: false
        }

        usdPairs.forEach { pool ->
            val usdShare = pool.breakdown?.find {
                stablecoinPriceProvider.isStable(pool.network.toNetwork()).invoke(it.token.address)
            }

            val otherShare = pool.breakdown?.find { share ->
                !stablecoinPriceProvider.isStable(pool.network.toNetwork()).invoke(share.token.address)
            }

            if (usdShare != null && otherShare != null && usdShare.reserveDecimal > BigDecimal.valueOf(10000)) {
                prices.add(
                    ExternalPrice(
                        usdShare.token.address.lowercase(),
                        pool.network.toNetwork(),
                        BigDecimal.valueOf(1.0),
                        "decentrifi-alienbase",
                        pool.name,
                        importOrder()
                    )
                )

                if (otherShare.reserve > BigInteger.ZERO) {
                    val otherprice = usdShare.reserve.asEth(usdShare.token.decimals).dividePrecisely(
                        otherShare.reserve.asEth(otherShare.token.decimals)
                    )
                    prices.add(
                        ExternalPrice(
                            otherShare.token.address,
                            pool.network.toNetwork(),
                            otherprice,
                            "decentrifi-alienbase",
                            pool.name,
                            importOrder()
                        )
                    )
                }
            }
        }
    }

    suspend fun getAlienbasePools(): List<PoolingMarketInformationDTO> {
        return markets.getPoolingMarkets("alienbase")
    }

    override fun importOrder(): Int = 30
}