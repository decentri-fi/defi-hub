package io.defitrack.protocol.sushiswap.pooling

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.contract.LPTokenContract
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.contract.SushiV2FactoryContract
import kotlinx.coroutines.flow.channelFlow

abstract class DefaultSushiPoolingMarketProvider(
    private val factoryAddress: String
) : PoolingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    //TODO: don't refetch all uniswap/sushiswap data every few days, just refresh it once a day

    override suspend fun produceMarkets() = channelFlow {

        val factory = createContract {
            SushiV2FactoryContract(factoryAddress)
        }

        factory.allPairs()
            .map {
                createContract {
                    LPTokenContract(it)
                }
            }.resolve()
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

    private suspend fun createMarket(lpToken: LPTokenContract): Either<Throwable, PoolingMarket> {
        return catch {

            val token0 = getToken(lpToken.token0.await())
            val token1 = getToken(lpToken.token1.await())

            val breakdown = refreshable {
                breakdownOf(
                    lpToken.address,
                    token0, token1
                )
            }

            create(
                address = lpToken.address,
                name = breakdown.get().joinToString("/") { it.token.name },
                symbol = breakdown.get().joinToString("/") { it.token.symbol },
                breakdown = breakdown,
                identifier = lpToken.address,
                positionFetcher = defaultPositionFetcher(lpToken.address),
                totalSupply = lpToken.totalDecimalSupply()
            )
        }
    }
}