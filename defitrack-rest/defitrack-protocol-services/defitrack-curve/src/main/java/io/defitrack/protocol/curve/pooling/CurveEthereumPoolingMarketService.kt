package io.defitrack.protocol.curve.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurveEthereumGraphProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class CurveEthereumPoolingMarketService(
    private val curveEthereumGraphProvider: CurveEthereumGraphProvider,
    private val erc20Resource: ERC20Resource
) : PoolingMarketProvider() {

    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return curveEthereumGraphProvider.getPools().map { pool ->

            val tokens = pool.coins.map { coin ->
                erc20Resource.getTokenInformation(getNetwork(), coin.underlying.token.address)
            }.map { it.toFungibleToken() }

            PoolingMarketElement(
                id = "curve-ethereum-${pool.lpToken.address}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = pool.lpToken.address,
                name = pool.lpToken.name,
                symbol = tokens.joinToString("/") { it.symbol },
                tokens = tokens,
                apr = BigDecimal.ZERO,
                marketSize = BigDecimal.ZERO,
                tokenType = TokenType.CURVE
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}