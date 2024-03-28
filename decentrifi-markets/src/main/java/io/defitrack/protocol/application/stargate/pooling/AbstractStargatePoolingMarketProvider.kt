package io.defitrack.protocol.application.stargate.pooling

import arrow.core.nel
import arrow.fx.coroutines.parMap
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.asShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.stargate.StargateService
import io.defitrack.protocol.stargate.contract.StargatePool
import io.defitrack.protocol.stargate.contract.StargatePoolFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

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

        pools.parMap(concurrency = 12) {
            with(getBlockchainGateway()) { StargatePool(it) }
        }.resolve().map { pool ->

            val token = getToken(pool.address)
            val underlying = getToken(pool.token.await())

            create(
                name = token.name,
                identifier = pool.address,
                address = pool.address,
                symbol = token.symbol,
                decimals = token.decimals,
                totalSupply = refreshable(token.totalDecimalSupply()) {
                    getToken(pool.address).totalDecimalSupply()
                },
                breakdown = refreshable {
                    underlying.asShare(pool.totalLiquidity()).nel()
                }
            )
        }.forEach { send(it) }
    }
}