package io.defitrack.protocol.sushiswap.apr

import io.defitrack.common.network.Network
import io.defitrack.protocol.SushiswapService
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class SushiswapAPRService(
    private val sushiServices: List<SushiswapService>,
) {

    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(Duration.Companion.hours(10)).build<String, BigDecimal>()

    suspend fun getAPR(address: String, network: Network): BigDecimal {
        return cache.get("$address-${network.slug}") {
            try {
                val pairData = getSushiswapService(network).getPairDayData(address)
                if (pairData.size <= 1) {
                    BigDecimal.ZERO
                } else {
                    pairData.drop(1).map {
                        it.volumeUSD
                    }.reduce { a, b -> a.plus(b) }
                        .times(BigDecimal.valueOf(0.0025)).times(BigDecimal.valueOf(52))
                        .divide(
                            getSushiswapService(network).getPairs().find {
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

    fun getSushiswapService(network: Network): SushiswapService {
        return sushiServices.find {
            it.getNetwork() == network
        } ?: throw IllegalArgumentException("Not found")
    }
}