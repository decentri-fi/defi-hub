package io.defitrack.protocol.application.curve.pooling

import arrow.core.Either
import arrow.core.Either.Companion.catch
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
            catch {
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

        val poolAsERC20 = getToken(pool)

        val breakdown = refreshable {
            breakdownOf(
                pool,
                *underlyingTokens.toTypedArray()
            )
        }

        return create(
            name = poolAsERC20.name,
            address = pool,
            identifier = createId(pool),
            symbol = poolAsERC20.symbol,
            tokens = underlyingTokens,
            breakdown = breakdown,
            totalSupply = refreshable {
                getToken(pool).totalDecimalSupply()
            }
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }
}