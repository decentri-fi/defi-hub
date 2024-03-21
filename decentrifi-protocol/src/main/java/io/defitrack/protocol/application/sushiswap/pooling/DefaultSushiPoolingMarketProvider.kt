package io.defitrack.protocol.sushiswap.pooling

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import io.defitrack.common.utils.refreshable
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.contract.SushiV2FactoryContract
import kotlinx.coroutines.flow.channelFlow
import java.util.concurrent.Flow

abstract class DefaultSushiPoolingMarketProvider(
    private val factoryAddress: String
) : PoolingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override suspend fun produceMarkets() = channelFlow {

        val factory = createContract {
            SushiV2FactoryContract(factoryAddress)
        }

        factory.allPairs()
            .parMap(concurrency = 12) {
                createMarket(it)
            }.mapNotNull {
                it.mapLeft {
                    logger.error("Failed to create market: {}", it.message)
                }.getOrNull()
            }.forEach {
                send(it)
            }
    }

    private suspend fun createMarket(lpAddress: String): Either<Throwable, PoolingMarket> {
        return catch {
            val token = getToken(lpAddress)

            val breakdown = breakdownOf(
                token.address,
                *token.underlyingTokens.toTypedArray()
            )

            create(
                address = lpAddress,
                name = token.name,
                symbol = token.symbol,
                tokens = token.underlyingTokens,
                breakdown = refreshable { breakdown },
                identifier = lpAddress,
                positionFetcher = defaultPositionFetcher(token.address),
                totalSupply = refreshable(token.totalDecimalSupply()) {
                    getToken(lpAddress).totalDecimalSupply()
                }
            )
        }
    }
}