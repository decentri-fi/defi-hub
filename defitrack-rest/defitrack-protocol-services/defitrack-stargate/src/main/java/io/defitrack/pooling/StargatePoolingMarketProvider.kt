package io.defitrack.pooling

import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.StargateService
import io.defitrack.protocol.contract.StargatePool
import io.defitrack.protocol.contract.StargatePoolFactory
import io.defitrack.token.TokenType
import kotlinx.coroutines.coroutineScope

abstract class StargatePoolingMarketProvider(
    stargateService: StargateService
) : PoolingMarketProvider() {

    val factory by lazy {
        StargatePoolFactory(
            getBlockchainGateway(),
            stargateService.getPoolFactory()
        )
    }

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        val pools = factory.getPools().filter {
            it != "0x0000000000000000000000000000000000000000"
        }
        pools.map {
            val pool = StargatePool(
                getBlockchainGateway(),
                it
            )

            val underlying = getToken(pool.token())

            create(
                name = pool.name(),
                identifier = pool.address,
                address = pool.address,
                symbol = pool.symbol(),
                tokenType = TokenType.STARGATE,
                tokens = listOf(underlying.toFungibleToken()),
                totalSupply = pool.totalSupply(),
            )
        }
    }
}