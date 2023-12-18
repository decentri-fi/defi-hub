package io.defitrack.protocol.stargate.pooling

import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
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
        val token = getToken(address)
        val underlying = getToken("0x0")
        send(
            create(
                name = "SGETH Token",
                identifier = "sgeth",
                marketSize = refreshable {
                    getPriceResource().calculatePrice(
                        PriceRequest(
                            "0x0",
                            getNetwork(),
                            getBlockchainGateway().getNativeBalance(address)
                        )
                    ).toBigDecimal()
                },
                address = address,
                symbol = "sgeth",
                tokens = listOf(underlying),
                totalSupply = refreshable{
                    getToken(address).totalDecimalSupply()
                }
            )
        )
    }
}