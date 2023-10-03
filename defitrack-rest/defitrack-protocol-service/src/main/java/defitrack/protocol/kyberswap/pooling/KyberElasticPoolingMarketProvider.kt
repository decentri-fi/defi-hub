package io.defitrack.protocol.kyberswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.kyberswap.contract.KyberswapElasticContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.KYBER_SWAP)
class KyberElasticPoolingMarketProvider(

) : PoolingMarketProvider() {

    val kyberswapElastic = lazyAsync {
            KyberswapElasticContract(
                getBlockchainGateway(),
                "0xb85ebe2e4ea27526f817ff33fb55fb240057c03f"
            )
    }

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        kyberswapElastic.await().allPairs().map { poolInfo ->
            async {
                val poolingToken = getToken(poolInfo.address)
                val tokens = poolingToken.underlyingTokens.map {
                    it.toFungibleToken()
                }

                try {
                    val breakdown = fiftyFiftyBreakdown(tokens[0], tokens[1], poolingToken.address)
                    create(
                        identifier = poolInfo.address,
                        marketSize = refreshable(breakdown.sumOf { it.reserveUSD }) {
                            fiftyFiftyBreakdown(tokens[0], tokens[1], poolingToken.address).sumOf {
                                it.reserveUSD
                            }
                        },
                        address = poolInfo.address,
                        name = poolingToken.name,
                        breakdown = breakdown,
                        symbol = poolingToken.symbol,
                        tokens = poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
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