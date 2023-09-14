package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class SolidLizardPoolingMarketProvider : PoolingMarketProvider() {
    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        val factory = PairFactoryContract(
            getBlockchainGateway(), "0x734d84631f00dc0d3fcd18b04b6cf42bfd407074"
        )

        return@coroutineScope factory.allPairs().map {
            async {

                val token = getToken(it)
                val tokens = token.underlyingTokens

                try {
                    create(
                        name = token.name,
                        identifier = token.address,
                        marketSize = refreshable {
                            getMarketSize(
                                tokens.map(TokenInformationVO::toFungibleToken),
                                it
                            )
                        },
                        positionFetcher = defaultPositionFetcher(token.address),
                        tokenType = TokenType.SOLIDLIZARD,
                        tokens = token.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                        symbol = token.symbol,
                        breakdown = defaultBreakdown(tokens, token.address),
                        address = token.address,
                        totalSupply = refreshable(token.totalSupply.asEth(token.decimals)) {
                            getToken(it).totalSupply.asEth(token.decimals)
                        }
                    )
                } catch (ex: Exception) {
                    logger.error("Error while fetching pooling market $it", ex)
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SOLIDLIZARD
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}