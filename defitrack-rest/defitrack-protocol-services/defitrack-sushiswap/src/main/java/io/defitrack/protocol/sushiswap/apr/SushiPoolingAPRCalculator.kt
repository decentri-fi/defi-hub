package io.defitrack.protocol.sushiswap.apr

import io.defitrack.apr.PoolingAprCalculator
import io.defitrack.protocol.SushiswapService
import java.math.BigDecimal

class SushiPoolingAPRCalculator(
    private val sushiswapService: SushiswapService,
    private val poolAddress: String
) : PoolingAprCalculator() {
    override fun getYearlyRewards(): BigDecimal {

        val pairData = sushiswapService.getPairDayData(poolAddress)
        return if (pairData.size <= 1) {
            BigDecimal.ZERO
        } else {
            pairData.drop(1).map {
                it.volumeUSD
            }.reduce { a, b -> a.plus(b) }
                .times(BigDecimal.valueOf(0.0025)).times(BigDecimal.valueOf(52))
        }
    }

    override fun getTvl(): BigDecimal {
        return sushiswapService.getPairs().find {
            it.id == poolAddress
        }?.reserveUSD ?: BigDecimal.ZERO
    }
}