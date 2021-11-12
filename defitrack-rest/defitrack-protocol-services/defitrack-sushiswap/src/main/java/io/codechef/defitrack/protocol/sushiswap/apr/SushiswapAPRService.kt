package io.codechef.defitrack.protocol.sushiswap.apr

import io.codechef.common.network.Network
import io.codechef.protocol.SushiswapService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class SushiswapAPRService(
    private val sushiServices: List<SushiswapService>,
) {

    @Cacheable(cacheNames = ["sushiswap-aprs"], key = "#address+'-'+#network")
    fun getAPR(address: String, network: Network): BigDecimal {
        try {
            val pairData = getSushiswapService(network).getPairDayData(address)
            return if (pairData.size <= 1) {
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
            return BigDecimal.ZERO
        }
    }

    fun getSushiswapService(network: Network): SushiswapService {
        return sushiServices.find {
            it.getNetwork() == network
        } ?: throw IllegalArgumentException("Not found")
    }
}