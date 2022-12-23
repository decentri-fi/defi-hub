package io.defitrack.protocol.uniswap.v3

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.MarketSizeService
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v3.UniswapV3Service
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import org.springframework.stereotype.Component

@Component
class UniswapV3EthereumPoolingMarketProvider(
    private val uniswapV3Service: UniswapV3Service,
    private val marketSizeService: MarketSizeService,
    erC20Resource: ERC20Resource,
) : PoolingMarketProvider(erC20Resource) {

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope{
        uniswapV3Service.providePools().map {
            async {
                try {
                    val token = erc20Resource.getTokenInformation(getNetwork(), it.id)
                    val token0 = erc20Resource.getTokenInformation(getNetwork(), it.token0.id)
                    val token1 = erc20Resource.getTokenInformation(getNetwork(), it.token1.id)
                    PoolingMarket(
                        network = getNetwork(),
                        protocol = getProtocol(),
                        id = "uniswap-v3-ethereum-${it.id}",
                        name = token.name,
                        address = it.id,
                        symbol = token.symbol,
                        tokens = listOf(
                            token0.toFungibleToken(),
                            token1.toFungibleToken()
                        ),
                        apr = null,
                        marketSize = marketSizeService.getMarketSize(token0.toFungibleToken(), it.id, getNetwork()).plus(
                            marketSizeService.getMarketSize(token1.toFungibleToken(), it.id, getNetwork())
                        ),
                        tokenType = TokenType.UNISWAP,
                        positionFetcher = defaultBalanceFetcher(token.address)
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    logger.error("something went wrong trying to import uniswap market ${it.id}")
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.UNISWAP
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}