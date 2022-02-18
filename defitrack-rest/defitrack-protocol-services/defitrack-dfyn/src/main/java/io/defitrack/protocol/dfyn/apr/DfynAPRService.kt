package io.defitrack.protocol.dfyn.apr

import io.defitrack.protocol.dfyn.DfynService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

@Component
class DfynAPRService(private val dfynService: DfynService) {

    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(10.hours).build<String, BigDecimal>()

    fun getAPR(address: String): BigDecimal = runBlocking {
        cache.get(address) {
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