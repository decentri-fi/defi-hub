package io.defitrack.protocol.apeswap

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
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

    val pools = lazyAsync {
        val pairFactoryContract = PairFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = apeswapPolygonService.provideFactory()
        )
        pairFactoryContract.allPairs()
    }


    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return pools.await().parMapNotNull(concurrency = 12) { pool ->
            try {
                val poolingToken = getToken(pool)
                val underlyingTokens = poolingToken.underlyingTokens

                val breakdown = refreshable {
                    fiftyFiftyBreakdown(underlyingTokens[0], underlyingTokens[1], poolingToken.address)
                }
                create(
                    identifier = pool,
                    address = pool,
                    name = poolingToken.name,
                    symbol = poolingToken.symbol,
                    breakdown = breakdown,
                    tokens = underlyingTokens,
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