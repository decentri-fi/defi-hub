package io.defitrack.protocol.stargate.pooling

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.map
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.stargate.StargateService
import io.defitrack.protocol.stargate.contract.StargatePool
import io.defitrack.protocol.stargate.contract.StargatePoolFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

abstract class AbstractStargatePoolingMarketProvider(
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
                            name = pool.readName(),
                            identifier = pool.address,
                            address = pool.address,
                            symbol = pool.readSymbol(),
                            tokens = listOf(underlying.toFungibleToken()),
                            decimals = pool.decimals(),
                            totalSupply = pool.totalSupply().map {
                                it.asEth(pool.decimals())
                            },
                            marketSize = Refreshable.refreshable {
                                getPriceResource().calculatePrice(
                                    PriceRequest(
                                        underlying.address,
                                        getNetwork(),
                                        pool.totalLiquidity().asEth(underlying.decimals),
                                    )
                                ).toBigDecimal()
                            }
                        )
                    )
                }
            }
        }
    }
}