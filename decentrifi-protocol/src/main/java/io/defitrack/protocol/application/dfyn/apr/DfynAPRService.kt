package io.defitrack.protocol.application.dfyn.apr

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.dfyn.DfynService
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration.Companion.hours

@Component
@ConditionalOnCompany(Company.DFYN)
class DfynAPRService(private val dfynService: DfynService) {

    val cache = Cache.Builder<String, BigDecimal>().expireAfterWrite(10.hours).build()

    suspend fun getAPR(address: String): BigDecimal  {
        return cache.get(address) {
            try {
                val pairData = dfynService.getPairDayData(address)
                if (pairData.size <= 1) {
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
                BigDecimal.ZERO
            }
        }
    }
}