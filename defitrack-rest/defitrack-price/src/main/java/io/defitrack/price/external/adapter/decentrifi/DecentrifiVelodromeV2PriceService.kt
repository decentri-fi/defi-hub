package io.defitrack.price.external.adapter.decentrifi

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.domain.pooling.PoolingMarketInformation
import io.defitrack.marketinfo.port.out.Markets
import io.defitrack.price.external.adapter.stable.StablecoinPriceProvider
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.out.ExternalPriceService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger

@Component
@ConditionalOnProperty("oracles.velodrome_v2.enabled", havingValue = "true", matchIfMissing = true)
class DecentrifiVelodromeV2PriceService(
    private val markets: Markets,
    private val stablecoinPriceProvider: StablecoinPriceProvider,
) : ExternalPriceService {


    val weth = "0x4200000000000000000000000000000000000006"

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return getPrices()
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val prices = mutableListOf<ExternalPrice>()

    fun getPrices() = runBlocking {
        val pools = getUniswapV2Pools()

        importNonStableUsdPairs(pools)
        importEthPairs(pools)
        logger.info("Decentri Velodrome V2 Underlying Price Repository populated with ${prices.size} prices")
        prices
    }

    private suspend fun importNonStableUsdPairs(pools: List<PoolingMarketInformation>) {
        val usdPairs = pools.filter { pool ->
            pool.breakdown?.any { share ->
                stablecoinPriceProvider.isStable(pool.network.toNetwork()).invoke(share.token.address)
            } ?: false
        }


        usdPairs.filter {
            it.metadata.getOrDefault("stable", "false").toString() == "false"
        }.forEach { pool ->
            val usdShare = pool.breakdown?.find {
                stablecoinPriceProvider.isStable(pool.network.toNetwork()).invoke(it.token.address)
            }

            val otherShare = pool.breakdown?.find { share ->
                !stablecoinPriceProvider.isStable(pool.network.toNetwork()).invoke(share.token.address)
            }

            if (usdShare != null && otherShare != null) {
                prices.add(
                    ExternalPrice(
                        usdShare.token.address.lowercase(),
                        pool.network.toNetwork(),
                        BigDecimal.ONE,
                        "velodrome-v2",
                        pool.name,
                        order()
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
                            "velodrome-v2",
                            pool.name,
                            order()
                        )
                    )
                }
            }
        }
    }

    private suspend fun importEthPairs(pools: List<PoolingMarketInformation>) {
        val usdPairs = pools.filter { pool ->
            pool.breakdown?.any { share ->
                share.token.address.lowercase() == weth
            } ?: false
        }


        usdPairs.filter {
            it.metadata.getOrDefault("stable", "false").toString() == "false"
        }.forEach { pool ->
            val ethShare = pool.breakdown?.find { share ->
                share.token.address.lowercase() == weth
            }

            val otherShare = pool.breakdown?.find { share ->
                share.token.address.lowercase() != weth
            }

            val ethPrice = prices.find {
                it.address.lowercase() == weth
            } ?: throw IllegalStateException("ETH price not found")

            if (ethShare != null && otherShare != null && otherShare.reserve > BigInteger.ZERO) {
                val otherprice = ethShare.reserve.asEth(ethShare.token.decimals)
                    .times(ethPrice.price)
                    .dividePrecisely(
                        otherShare.reserve.asEth(otherShare.token.decimals)
                    )
                prices.add(
                    ExternalPrice(
                        otherShare.token.address,
                        pool.network.toNetwork(),
                        otherprice,
                        "velodrome-v2",
                        pool.name,
                        order()
                    )
                )
            }
        }
    }


    suspend fun getUniswapV2Pools(): List<PoolingMarketInformation> {
        return markets.getPoolingMarkets("velodrome_v2")
    }
}