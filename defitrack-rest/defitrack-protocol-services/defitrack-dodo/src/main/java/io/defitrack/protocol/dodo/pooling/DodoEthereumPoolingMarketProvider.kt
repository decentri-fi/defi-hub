package io.defitrack.protocol.dodo.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.DodoEthereumGraphProvider
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service

@Service
class DodoEthereumPoolingMarketProvider(
    private val erC20Resource: ERC20Resource,
    private val dodoEthereumGraphProvider: DodoEthereumGraphProvider,
) : PoolingMarketService() {
    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return dodoEthereumGraphProvider.getPools().map { pool ->
            val underlying = pool.inputTokens.map {
                val tokens = erC20Resource.getTokenInformation(getNetwork(), it.id)
                tokens.toFungibleToken()
            }

            PoolingMarketElement(
                id = "dodo-ethereum-${pool.id}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = pool.id,
                name = underlying.joinToString("/") { it.symbol } + " LP",
                symbol = underlying.joinToString("/") { it.symbol },
                tokens = underlying,
                tokenType = TokenType.DODO
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.DODO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}