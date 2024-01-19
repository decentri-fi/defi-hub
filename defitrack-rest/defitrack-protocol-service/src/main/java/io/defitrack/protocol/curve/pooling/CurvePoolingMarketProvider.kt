package io.defitrack.protocol.curve.pooling

import arrow.core.Either
import arrow.core.Option
import arrow.core.getOrElse
import arrow.fx.coroutines.parMap
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.contract.CurveFactoryContract
import io.defitrack.protocol.crv.contract.CurvePoolContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.math.BigDecimal

abstract class CurvePoolingMarketProvider(
    private val factoryAddress: String
) : PoolingMarketProvider() {

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val contract = CurveFactoryContract(
            getBlockchainGateway(),
            factoryAddress
        )

        val coinsPerPool = contract.getCoins()
        coinsPerPool.entries.parMap(concurrency = 12) {
            Either.catch {
                createMarket(it)
            }
        }.mapNotNull {
            it.mapLeft {
                logger.error("Error creating market: {}", it.message)
            }.getOrNull()
        }.forEach {
            send(it)
        }
    }

    private suspend fun createMarket(it: Map.Entry<String, List<String>>): PoolingMarket {
        val pool = it.key

        val underlyingTokens = it.value.filter { it != "0x0000000000000000000000000000000000000000" }.parMap {
            getToken(it)
        }

        val poolContract = CurvePoolContract(getBlockchainGateway(), pool)
        val poolAsERC20 = getToken(pool)

        return create(
            name = poolAsERC20.name,
            address = pool,
            identifier = createId(pool),
            symbol = poolAsERC20.symbol,
            tokens = underlyingTokens,
            totalSupply = refreshable {
                getToken(pool).totalDecimalSupply()
            },
            price = refreshable {
                Option.catch {
                    poolContract.virtualPrice.await().asEth(poolAsERC20.decimals)
                }.getOrElse { BigDecimal.ZERO }
            }
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }
}