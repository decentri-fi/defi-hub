package io.defitrack.pooling

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.StargateService
import io.defitrack.protocol.contract.StargatePool
import io.defitrack.protocol.contract.StargatePoolFactory
import io.defitrack.token.TokenType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

abstract class StargatePoolingMarketProvider(
    stargateService: StargateService
) : PoolingMarketProvider() {


    override fun getProtocol(): Protocol {
        return Protocol.STARGATE
    }

    val factory by lazy {
        StargatePoolFactory(
            getBlockchainGateway(),
            stargateService.getPoolFactory()
        )
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val pools = factory.getPools().filter {
            it != "0x0000000000000000000000000000000000000000"
        }

        pools.forEach {
            launch {
                throttled {
                    val pool = StargatePool(
                        getBlockchainGateway(),
                        it
                    )

                    val underlying = getToken(pool.token())

                    send(
                        create(
                            name = pool.name(),
                            identifier = pool.address,
                            address = pool.address,
                            symbol = pool.symbol(),
                            tokenType = TokenType.STARGATE,
                            tokens = listOf(underlying.toFungibleToken()),
                            decimals = pool.decimals(),
                            totalSupply = pool.totalSupply(),
                            marketSize = getPriceResource().calculatePrice(
                                PriceRequest(
                                    underlying.address,
                                    getNetwork(),
                                    pool.totalLiquidity().asEth(underlying.decimals),
                                )
                            ).toBigDecimal()
                        )
                    )
                }
            }
        }
    }
}