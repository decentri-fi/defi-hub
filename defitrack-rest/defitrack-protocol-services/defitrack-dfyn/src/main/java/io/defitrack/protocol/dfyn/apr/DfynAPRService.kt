package io.defitrack.protocol.dfyn.apr

import io.defitrack.protocol.dfyn.DfynService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class DfynAPRService(private val dfynService: DfynService) {

    @Cacheable(cacheNames = ["dfyn-aprs"], key = "#address")
    fun getAPR(address: String): BigDecimal {
        try {
            val pairData = dfynService.getPairDayData(address)
            return if (pairData.size <= 1) {
                BigDecimal.ZERO
            } else {
                pairData.drop(1).map {
                    it.dailyVolumeUSD
                }.reduce { a, b -> a.plus(b) }
                    .times(BigDecimal.valueOf(0.003)).times(BigDecimal.valueOf(52))
                    .divide(
                        dfynService.getPairs().find {
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
}