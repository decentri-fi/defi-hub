package io.defitrack.protocol.aave.lending

import io.codechef.defitrack.lending.LendingMarketService
import io.codechef.defitrack.lending.domain.LendingMarketElement
import io.codechef.defitrack.lending.domain.LendingToken
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AavePolygonService
import okhttp3.internal.toImmutableList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Service
class AavePolygonLendingMarketService(private val aavePolygonService: AavePolygonService) : LendingMarketService {

    val lendingMarketBuffer = mutableListOf<LendingMarketElement>()
    val executor: ExecutorService = Executors.newWorkStealingPool(8)

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @PostConstruct
    fun startup() {
        aavePolygonService.getReserves().forEach {
            executor.submit {
                val market = LendingMarketElement(
                    id = "polygon-aave-${it.symbol}",
                    address = it.underlyingAsset,
                    token = LendingToken(
                        name = it.name,
                        symbol = it.symbol,
                        address = it.underlyingAsset
                    ),
                    name = it.name + " Aave Pool",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    rate = it.lendingRate,
                    marketSize = 0.0,
                    poolType = "aave-v2"
                )
                logger.info("Adding ${market.name}")
                lendingMarketBuffer.add(market)
            }
        }
    }


    override fun getLendingMarkets(): List<LendingMarketElement> {
        return lendingMarketBuffer.toImmutableList()
    }

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.POLYGON
}