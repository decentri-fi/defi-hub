package io.defitrack.protocol.application.apeswap

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.apeswap.ApeswapPolygonService
import io.defitrack.uniswap.v2.PairFactoryContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.APESWAP)
class ApeswapPolygonPoolingMarketProvider(
    private val apeswapPolygonService: ApeswapPolygonService
) : PoolingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.APESWAP
    }

    private fun pairFactoryContract() =
        with(getBlockchainGateway()) { PairFactoryContract(apeswapPolygonService.provideFactory()) }

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return pairFactoryContract()
            .allPairs()
            .parMapNotNull(concurrency = 12) { pool ->
                try {
                    val poolingToken = getToken(pool)
                    val underlyingTokens = poolingToken.underlyingTokens

                    val breakdown = refreshable {
                        breakdownOf(poolingToken.address, underlyingTokens[0], underlyingTokens[1])
                    }
                    create(
                        identifier = pool,
                        address = pool,
                        name = poolingToken.name,
                        symbol = poolingToken.symbol,
                        breakdown = breakdown,
                        positionFetcher = defaultPositionFetcher(poolingToken.address),
                        totalSupply = refreshable {
                            getToken(pool).totalDecimalSupply()
                        }
                    )
                } catch (ex: Exception) {
                    logger.error("Error while fetching pooling market $pool", ex)
                    null
                }
            }
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}