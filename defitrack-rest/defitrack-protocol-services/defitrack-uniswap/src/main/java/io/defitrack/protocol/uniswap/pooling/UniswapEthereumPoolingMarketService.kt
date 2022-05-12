package io.defitrack.protocol.uniswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.uniswap.apr.UniswapAPRService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import io.defitrack.uniswap.AbstractUniswapV2Service
import org.springframework.stereotype.Component

@Component
class UniswapEthereumPoolingMarketService(
    private val uniswapServices: List<AbstractUniswapV2Service>,
    private val erC20Resource: ERC20Resource,
    private val uniswapAPRService: UniswapAPRService,
) : PoolingMarketService() {

    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return uniswapServices.filter {
            it.getNetwork() == getNetwork()
        }.flatMap {
            it.getPairs().mapNotNull {
                try {
                    val token = erC20Resource.getTokenInformation(getNetwork(), it.id)
                    val token0 = erC20Resource.getTokenInformation(getNetwork(), it.token0.id)
                    val token1 = erC20Resource.getTokenInformation(getNetwork(), it.token1.id)
                    PoolingMarketElement(
                        network = getNetwork(),
                        protocol = getProtocol(),
                        id = "uniswap-ethereum-${it.id}",
                        name = token.name,
                        address = it.id,
                        symbol = token.symbol,
                        token = listOf(
                            token0.toFungibleToken(),
                            token1.toFungibleToken()
                        ),
                        apr = uniswapAPRService.getAPR(it.id, getNetwork()),
                        marketSize = it.reserveUSD,
                        tokenType = TokenType.UNISWAP
                    )
                } catch (ex: Exception) {
                    logger.error("something went wrong trying to import uniswap market ${it.id}")
                    null
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.UNISWAP
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}