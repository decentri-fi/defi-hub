package io.defitrack

import io.defitrack.common.network.Network
import io.defitrack.protocol.SpiritswapService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.ExperimentalTime

@Component
class SpiritswapAPRService(
    private val spiritswapServices: List<SpiritswapService>,
) {

    @OptIn(ExperimentalTime::class)
    val aprCache = Cache.Builder().expireAfterWrite(
        kotlin.time.Duration.Companion.hours(4)).build<String, BigDecimal>()

    fun getAPR(address: String, network: Network): BigDecimal {
        return runBlocking {
            aprCache.get("${address}_${network.slug}") {
                try {
                    val pairData = getSpiritswapService(network).getPairDayData(address)
                    if (pairData.size <= 1) {
                        BigDecimal.ZERO
                    } else {
                        pairData.drop(1).map {
                            it.dailyVolumeUSD
                        }.reduce { a, b -> a.plus(b) }
                            .times(BigDecimal.valueOf(0.0025)).times(BigDecimal.valueOf(52))
                            .divide(
                                getSpiritswapService(network).getPairs().find {
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

    fun getSpiritswapService(network: Network): SpiritswapService {
        return spiritswapServices.find {
            it.getNetwork() == network
        } ?: throw IllegalArgumentException("Not found")
    }
}