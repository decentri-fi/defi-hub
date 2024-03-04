package io.defitrack.protocol.application.camelot.pooling

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

//@Component
class CamelotArbitrumPoolingMarketProvider : PoolingMarketProvider() {


    //todo: use parmaps
    override suspend fun produceMarkets(): Flow<PoolingMarket> {
        return channelFlow {
            pairFactoryContract()
                .allPairs()
                .parMapNotNull(concurrency = 12) { pool ->
                    catch {
                        createPoolingMarket(pool)
                    }
                        .mapLeft { logger.error("unable to create market: {}", it.message) }
                        .getOrNull()
                }.forEach {
                    send(it)
                }
        }
    }

    private suspend fun createPoolingMarket(
        pool: String
    ): PoolingMarket {
        val poolingToken = getToken(pool)
        val underlyingTokens = poolingToken.underlyingTokens


        val breakdown = refreshable {
            fiftyFiftyBreakdown(underlyingTokens[0], underlyingTokens[1], poolingToken.address)
        }

        return create(
            identifier = pool,
            address = pool,
            name = poolingToken.name,
            symbol = poolingToken.symbol,
            breakdown = breakdown,
            positionFetcher = defaultPositionFetcher(poolingToken.address),
            totalSupply = refreshable(poolingToken.totalDecimalSupply()) {
                getToken(pool).totalDecimalSupply()
            },
        )
    }

    private fun pairFactoryContract() = with(getBlockchainGateway()) {
        PairFactoryContract("0x6eccab422d763ac031210895c81787e87b43a652")
    }


    override fun getProtocol(): Protocol {
        return Protocol.CAMELOT
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}