package io.defitrack.protocol.velodrome.pooling

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.port.out.PoolingMarketProvider
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
        getPairFactoryContract()
            .allPairs()
            .parMap(concurrency = 12) {
                createMarket(it)
            }.forEach {
                it.fold(
                    { throwable -> logger.error("Error creating market", throwable) },
                    { poolingMarket -> send(poolingMarket) }
                )
            }
    }

    private fun getPairFactoryContract() =
        with(getBlockchainGateway()) { PairFactoryContract(velodromeOptimismService.getV1PoolFactory()) }

    private suspend fun createMarket(it: String): Either<Throwable, PoolingMarket> {
        return catch {
            val poolingToken = getToken(it)
            val tokens = poolingToken.underlyingTokens

            val breakdown = refreshable {
                breakdownOf(poolingToken.address, tokens[0], tokens[1])
            }

            create(
                identifier = "v1-$it",
                positionFetcher = defaultPositionFetcher(poolingToken.address),
                address = it,
                name = poolingToken.name,
                breakdown = breakdown,
                symbol = poolingToken.symbol,
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