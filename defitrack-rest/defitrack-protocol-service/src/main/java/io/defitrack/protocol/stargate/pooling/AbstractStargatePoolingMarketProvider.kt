package io.defitrack.protocol.stargate.pooling

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.stargate.StargateService
import io.defitrack.protocol.stargate.contract.StargatePool
import io.defitrack.protocol.stargate.contract.StargatePoolFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

abstract class AbstractStargatePoolingMarketProvider(
    private val stargateService: StargateService
) : PoolingMarketProvider() {


    override fun getProtocol(): Protocol {
        return Protocol.STARGATE
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val pools = StargatePoolFactory(
            getBlockchainGateway(),
            stargateService.getPoolFactory()
        ).getPools().filter {
            it != "0x0000000000000000000000000000000000000000"
        }

        //TODO
        pools.forEach {
            launch {
                throttled {
                    val pool = StargatePool(
                        getBlockchainGateway(),
                        it
                    )

                    val token = getToken(it)

                    val underlying = getToken(pool.token())

                    send(
                        create(
                            name = token.name,
                            identifier = pool.address,
                            address = pool.address,
                            symbol = token.symbol,
                            tokens = listOf(underlying),
                            decimals = token.decimals,
                            totalSupply = pool.totalSupply().map {
                                it.asEth(pool.readDecimals())
                            },
                            marketSize = refreshable {
                                getPriceResource().calculatePrice(
                                    GetPriceCommand(
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