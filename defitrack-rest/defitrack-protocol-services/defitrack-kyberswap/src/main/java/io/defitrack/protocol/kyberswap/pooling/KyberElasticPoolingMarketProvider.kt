package io.defitrack.protocol.kyberswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.kyberswap.contract.KyberswapElasticContract
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class KyberElasticPoolingMarketProvider(

) : PoolingMarketProvider() {

    val kyberswapElastic by lazy {
        runBlocking {
            KyberswapElasticContract(
                getBlockchainGateway(),
                "0xb85ebe2e4ea27526f817ff33fb55fb240057c03f"
            )
        }
    }

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        kyberswapElastic.allPairs().map { poolInfo ->
            async {
                val poolingToken = getToken(poolInfo.address)
                val tokens = poolingToken.underlyingTokens

                try {
                    create(
                        identifier = poolInfo.address,
                        marketSize = refreshable {
                            getMarketSize(
                                poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                                poolInfo.address
                            )
                        },
                        address = poolInfo.address,
                        name = poolingToken.name,
                        breakdown = defaultBreakdown(tokens, poolingToken.address),
                        symbol = poolingToken.symbol,
                        tokens = poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                        tokenType = TokenType.VELODROME,
                        totalSupply = refreshable(poolingToken.totalSupply.asEth(poolingToken.decimals)) {
                            val token = getToken(poolInfo.address)
                            token.totalSupply.asEth(token.decimals)
                        },
                    )
                } catch (ex: Exception) {
                    logger.error("Error while fetching pooling market ${poolInfo.address}", ex)
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.KYBER_SWAP
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}