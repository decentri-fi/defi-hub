package io.defitrack.protocol.kyberswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.kyberswap.apr.KyberswapAPRService
import io.defitrack.protocol.kyberswap.graph.KyberswapEthereumGraphProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.KYBER_SWAP)
class KyberswapEthereumPoolingMarketProvider(
    private val kyberswapPolygonService: KyberswapEthereumGraphProvider,
    private val kyberswapAPRService: KyberswapAPRService,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        kyberswapPolygonService.getPoolingMarkets().map {
            async {
                throttled {
                    try {
                        val token = getToken(it.id)
                        val token0 = getToken(it.token0.id)
                        val token1 = getToken(it.token1.id)

                        create(
                            identifier = it.id,
                            address = it.id,
                            name = token.name,
                            symbol = token.symbol,
                            tokens = listOf(
                                token0.toFungibleToken(),
                                token1.toFungibleToken()
                            ),
                            apr = kyberswapAPRService.getAPR(it.pair.id, getNetwork()),
                            marketSize = refreshable(it.reserveUSD),
                            positionFetcher = defaultPositionFetcher(token.address),
                            totalSupply = refreshable(token.totalSupply.asEth(token.decimals)) {
                                getToken(it.id).totalDecimalSupply()
                            }
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        null
                    }
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