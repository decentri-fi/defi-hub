package io.codechef.defitrack.protocol.dmm.apr

import io.defitrack.common.network.Network
import io.defitrack.protocol.dmm.DMMEthereumService
import io.defitrack.protocol.dmm.DMMPairDayData
import io.defitrack.protocol.dmm.DMMPolygonService
import io.defitrack.protocol.dmm.DMMPool
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class DMMAPRService(
    private val dmmPolygonService: DMMPolygonService,
    private val dmmEthereumService: DMMEthereumService
) {

    @Cacheable(cacheNames = ["dmm-aprs"], key = "#address+'-'+#network")
    fun getAPR(address: String, network: Network): BigDecimal {
        try {
            val pairData = getPairData(address, network);
            return if (pairData.size <= 1) {
                BigDecimal.ZERO
            } else {
                pairData.drop(1).map {
                    it.dailyVolumeUSD
                }.reduce { a, b -> a.plus(b) }
                    .times(BigDecimal.valueOf(0.0025)).times(BigDecimal.valueOf(52))
                    .divide(
                        getPools(network).find {
                            it.pair.id == address
                        }!!.reserveUSD,
                        18,
                        RoundingMode.HALF_UP
                    )
            }
        } catch (ex: Exception) {
            return BigDecimal.ZERO
        }
    }

    private fun getPairData(address: String, network: Network): List<DMMPairDayData> {
        return when (network) {
            Network.POLYGON -> dmmPolygonService.getPairDayData(address)
            Network.ETHEREUM -> return dmmEthereumService.getPairDayData(address)
            else -> emptyList()
        }
    }

    fun getPools(network: Network): List<DMMPool> {
        return when (network) {
            Network.POLYGON -> return dmmPolygonService.getPoolingMarkets()
            Network.ETHEREUM -> return dmmEthereumService.getPoolingMarkets()
            else -> emptyList()
        }
    }
}