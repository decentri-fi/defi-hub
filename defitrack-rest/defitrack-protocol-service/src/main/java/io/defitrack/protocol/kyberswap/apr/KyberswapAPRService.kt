package io.defitrack.protocol.kyberswap.apr

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.kyberswap.graph.KyberswapEthereumGraphProvider
import io.defitrack.protocol.kyberswap.graph.domain.PairDayData
import io.defitrack.protocol.kyberswap.graph.KyberswapPolygonGraphProvider
import io.defitrack.protocol.kyberswap.graph.domain.Pool
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration.Companion.hours

@Component
@ConditionalOnCompany(Company.KYBER_SWAP)
class KyberswapAPRService(
    private val kyberswapPolygonGraphProvider: KyberswapPolygonGraphProvider,
    private val kyberswapEthereumGraphProvider: KyberswapEthereumGraphProvider
) {

    val cache = Cache.Builder<String, BigDecimal>().expireAfterWrite(10.hours).build()

    suspend fun getAPR(address: String, network: Network): BigDecimal {
        return cache.get("$address-${network.slug}") {
            try {
                val pairData = getPairData(address, network)
                if (pairData.size <= 1) {
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
                BigDecimal.ZERO
            }
        }
    }

    private suspend fun getPairData(address: String, network: Network): List<PairDayData> {
        return when (network) {
            Network.POLYGON -> kyberswapPolygonGraphProvider.getPairDayData(address)
            Network.ETHEREUM -> return kyberswapEthereumGraphProvider.getPairDayData(address)
            else -> emptyList()
        }
    }

    suspend fun getPools(network: Network): List<Pool> {
        return when (network) {
            Network.POLYGON -> return kyberswapPolygonGraphProvider.getPoolingMarkets()
            Network.ETHEREUM -> return kyberswapEthereumGraphProvider.getPoolingMarkets()
            else -> emptyList()
        }
    }
}