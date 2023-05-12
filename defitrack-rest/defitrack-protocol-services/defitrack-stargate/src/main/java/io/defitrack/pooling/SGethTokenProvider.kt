package io.defitrack.pooling

import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

abstract class SGethTokenProvider(
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
                marketSize = getPriceResource().calculatePrice(
                    PriceRequest(
                        "0x0",
                        getNetwork(),
                        getBlockchainGateway().getNativeBalance(address)
                    )
                ).toBigDecimal(),
                address = address,
                symbol = "sgeth",
                tokenType = TokenType.STARGATE_VAULT,
                tokens = listOf(underlying.toFungibleToken()),
                totalSupply = token.totalSupply
            )
        )
    }
}