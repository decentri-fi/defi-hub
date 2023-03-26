package io.defitrack.protocol.uniswap.v2.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.uniswap.v2.apr.UniswapAPRService
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v2.AbstractUniswapV2Service
import org.springframework.stereotype.Component

@Component
class UniswapV2EthereumPoolingMarketProvider(
    private val uniswapServices: List<AbstractUniswapV2Service>,
    private val uniswapAPRService: UniswapAPRService,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return uniswapServices.filter {
            it.getNetwork() == getNetwork()
        }.flatMap { service ->
            service.getPairs().mapNotNull {
                try {
                    val token = getToken(it.id)
                    val token0 = getToken(it.token0.id)
                    val token1 = getToken(it.token1.id)
                    PoolingMarket(
                        network = getNetwork(),
                        protocol = getProtocol(),
                        id = "uniswap-v2-ethereum-${it.id}",
                        name = token.name,
                        address = it.id,
                        symbol = token.symbol,
                        tokens = listOf(
                            token0.toFungibleToken(),
                            token1.toFungibleToken()
                        ),
                        apr = uniswapAPRService.getAPR(it.id, getNetwork()),
                        marketSize = it.reserveUSD,
                        tokenType = TokenType.UNISWAP,
                        positionFetcher = defaultPositionFetcher(token.address),
                        totalSupply = token.totalSupply
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    logger.error("something went wrong trying to import uniswap market ${it.id}")
                    null
                }
            }
        }
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}