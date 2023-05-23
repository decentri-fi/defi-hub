package io.defitrack.protocol.uniswap.v2.apr

import io.defitrack.common.network.Network
import io.defitrack.uniswap.v2.AbstractUniswapV2Service
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration.Companion.hours

@Component
class UniswapAPRService(private val abstractUniswapV2Service: List<AbstractUniswapV2Service>) {

    val cache = Cache.Builder<String, BigDecimal>().expireAfterWrite(10.hours).build()

    fun getAPR(address: String, network: Network): BigDecimal = runBlocking {
        cache.get("$address-${network.slug}") {
            try {
                val pairData =
                    abstractUniswapV2Service.firstOrNull { it.getNetwork() == network }?.getPairDayData(address)
                        ?: emptyList()

                if (pairData.size <= 1) {
                    BigDecimal.ZERO
                } else {
                    pairData.drop(1).map {
                        it.dailyVolumeUSD
                    }.reduce { a, b -> a.plus(b) }
                        .times(BigDecimal.valueOf(0.003)).times(BigDecimal.valueOf(52))
                        .divide(
                            abstractUniswapV2Service.firstOrNull {
                                it.getNetwork() == network
                            }?.getPairs()!!.find {
                                it.id == address
                            }!!.reserveUSD,
                            18,
                            RoundingMode.HALF_UP
                        )
                }
            } catch (ex: Exception) {
                BigDecimal.ZERO
            }
        }
    }
}