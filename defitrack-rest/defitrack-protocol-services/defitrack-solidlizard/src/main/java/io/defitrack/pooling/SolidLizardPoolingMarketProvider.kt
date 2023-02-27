package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
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

                val poolingToken = getToken(it)
                val tokens = poolingToken.underlyingTokens

                try {
                    create(
                        name = poolingToken.name,
                        identifier = poolingToken.address,
                        marketSize = getMarketSize(
                            poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken), it
                        ),
                        positionFetcher = defaultPositionFetcher(poolingToken.address),
                        tokenType = TokenType.SOLIDLIZARD,
                        tokens = poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                        symbol = poolingToken.symbol,
                        breakdown = defaultBreakdown(tokens, poolingToken.address),
                        address = poolingToken.address
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