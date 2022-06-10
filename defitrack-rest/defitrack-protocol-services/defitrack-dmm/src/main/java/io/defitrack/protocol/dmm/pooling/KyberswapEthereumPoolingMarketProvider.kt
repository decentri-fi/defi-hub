package io.defitrack.protocol.dmm.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dmm.KyberswapEthereumGraphProvider
import io.defitrack.protocol.dmm.apr.KyberswapAPRService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class KyberswapEthereumPoolingMarketProvider(
    private val kyberswapPolygonService: KyberswapEthereumGraphProvider,
    private val kyberswapAPRService: KyberswapAPRService,
    private val erc20Resource: ERC20Resource
) : PoolingMarketService() {

    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> =
        withContext(Dispatchers.IO.limitedParallelism(10)) {
            kyberswapPolygonService.getPoolingMarkets().map {
                async {
                    try {
                        val token = erc20Resource.getTokenInformation(getNetwork(), it.id)
                        val token0 = erc20Resource.getTokenInformation(getNetwork(), it.token0.id)
                        val token1 = erc20Resource.getTokenInformation(getNetwork(), it.token1.id)

                        PoolingMarketElement(
                            id = "dmm-ethereum-${it.id}",
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
                            tokenType = TokenType.DMM
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
        return Network.ETHEREUM
    }
}