package io.defitrack.protocol.curve.pooling

import io.defitrack.common.utils.AsyncUtils
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.contract.CurveFactoryContract
import io.defitrack.protocol.crv.contract.CurvePoolContract
import io.defitrack.token.TokenType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.math.BigDecimal

abstract class CurvePoolingMarketProvider(
    private val factoryAddress: String
) : PoolingMarketProvider() {

    val factory = AsyncUtils.lazyAsync {
        CurveFactoryContract(
            getBlockchainGateway(),
            factoryAddress
        )
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val contract = factory.await()
        val coinsPerPool = contract.getCoins()
        val balances = contract.getBalances()
        coinsPerPool.forEach {
            val pool = it.key

            val underlyingTokens = it.value.filter { it != "0x0000000000000000000000000000000000000000" }.map {
                getToken(it)
            }

            val poolContract = CurvePoolContract(getBlockchainGateway(), pool)
            val poolAsERC20 = getToken(pool)

            val market = create(
                name = poolAsERC20.name,
                address = pool,
                identifier = createId(pool),
                symbol = poolAsERC20.symbol,
                tokens = underlyingTokens
                    .map { it.toFungibleToken() },
                totalSupply = Refreshable.refreshable(poolAsERC20.totalSupply.asEth(poolAsERC20.decimals)) {
                    getToken(pool).totalSupply.asEth(poolAsERC20.decimals)
                },
                price = Refreshable.refreshable {
                    try {
                        poolContract.virtualPrice.await().asEth(poolAsERC20.decimals)
                    } catch (ex: Exception) {
                        BigDecimal.ZERO
                    }
                }
            )

            send(market)
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }
}