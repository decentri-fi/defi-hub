package io.defitrack.protocol.dodo.pooling

import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.DodoGraphProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType

abstract class DodoPoolingMarketProvider(
    private val erC20Resource: ERC20Resource,
    private val dodoGraphProvider: DodoGraphProvider,
) : PoolingMarketService() {
    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return dodoGraphProvider.getPools().map { pool ->
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
}