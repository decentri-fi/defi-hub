package io.defitrack.protocol.kyberswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.kyberswap.graph.KyberswapEthereumGraphProvider
import io.defitrack.protocol.kyberswap.apr.KyberswapAPRService
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
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> =
        withContext(Dispatchers.IO.limitedParallelism(10)) {
            kyberswapPolygonService.getPoolingMarkets().map {
                async {
                    try {
                        val token = getToken(it.id)
                        val token0 = getToken(it.token0.id)
                        val token1 = getToken(it.token1.id)

                        PoolingMarket(
                            id = "kyberswap-ethereum-${it.id}",
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
                            positionFetcher = defaultPositionFetcher(token.address)
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