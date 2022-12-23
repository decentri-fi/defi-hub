package io.defitrack.protocol.kyberswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.kyberswap.KyberswapPolygonGraphProvider
import io.defitrack.protocol.kyberswap.apr.KyberswapAPRService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class KyberswapPolygonPoolingMarketProvider(
    private val kyberswapPolygonGraphProvider: KyberswapPolygonGraphProvider,
    private val kyberswapAPRService: KyberswapAPRService,
     erc20Resource: ERC20Resource
) : PoolingMarketProvider(erc20Resource) {

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        kyberswapPolygonGraphProvider.getPoolingMarkets().map {
            async {
                try {
                    val token = erc20Resource.getTokenInformation(getNetwork(), it.id)
                    val token0 = erc20Resource.getTokenInformation(getNetwork(), it.token0.id)
                    val token1 = erc20Resource.getTokenInformation(getNetwork(), it.token1.id)

                    PoolingMarket(
                        id = "kyberswap-polygon-${it.id}",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        address = it.id,
                        name = token.name,
                        symbol = token.symbol,
                        tokens = listOf(
                            token0.toFungibleToken(),
                            token1.toFungibleToken()
                        ),
                        apr = kyberswapAPRService.getAPR(it.pair.id, getNetwork()),
                        marketSize = it.reserveUSD,
                        tokenType = TokenType.KYBER,
                        positionFetcher = defaultBalanceFetcher(token.address)
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.KYBER_SWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}