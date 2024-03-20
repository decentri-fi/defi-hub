package io.defitrack.protocol.application.stargate.pooling

import arrow.core.nel
import io.defitrack.common.utils.refreshable
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

abstract class AbstractSGethTokenProvider(
    val address: String
) : PoolingMarketProvider() {


    override fun getProtocol(): Protocol {
        return Protocol.STARGATE
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val underlying = getToken("0x0")
        send(
            create(
                name = "SGETH Token",
                identifier = "sgeth",
                address = address,
                symbol = "sgeth",
                tokens = listOf(underlying),
                breakdown = refreshable {
                    PoolingMarketTokenShare(
                        underlying,
                        getToken(address).totalSupply
                    ).nel()
                },
                totalSupply = refreshable {
                    getToken(address).totalDecimalSupply()
                }
            )
        )
    }
}