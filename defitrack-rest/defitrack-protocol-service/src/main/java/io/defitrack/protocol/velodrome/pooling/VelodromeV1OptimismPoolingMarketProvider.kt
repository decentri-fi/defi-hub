package io.defitrack.protocol.velodrome.pooling

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare
import io.defitrack.market.pooling.domain.marketSize
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.velodrome.VelodromeOptimismService
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.VELODROME)
@ConditionalOnProperty(value = ["velodromev1.enabled"], havingValue = "true", matchIfMissing = true)
class VelodromeV1OptimismPoolingMarketProvider(
    private val velodromeOptimismService: VelodromeOptimismService
) : PoolingMarketProvider() {


    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val pairFactoryContract = PairFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = velodromeOptimismService.getV1PoolFactory()
        )

        pairFactoryContract.allPairs().parMap(EmptyCoroutineContext, 12) {
            createMarket(it)
        }.forEach {
            it.fold(
                { throwable -> logger.error("Error creating market", throwable) },
                { poolingMarket -> send(poolingMarket) }
            )
        }
    }

    private suspend fun createMarket(it: String): Either<Throwable, PoolingMarket> {
        return catch {
            val poolingToken = getToken(it)
            val tokens = poolingToken.underlyingTokens

            val breakdown = refreshable {
                fiftyFiftyBreakdown(tokens[0], tokens[1], poolingToken.address)
            }

            create(
                identifier = "v1-$it",
                marketSize = breakdown.map(List<PoolingMarketTokenShare>::marketSize),
                positionFetcher = defaultPositionFetcher(poolingToken.address),
                address = it,
                name = poolingToken.name,
                breakdown = breakdown,
                symbol = poolingToken.symbol,
                tokens = poolingToken.underlyingTokens,
                totalSupply = refreshable {
                    getToken(it).totalDecimalSupply()
                },
                deprecated = true
            )
        }
    }


    override fun getProtocol(): Protocol {
        return Protocol.VELODROME_V1
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}