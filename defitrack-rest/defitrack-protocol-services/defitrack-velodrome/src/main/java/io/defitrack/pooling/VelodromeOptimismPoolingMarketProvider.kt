package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.VelodromeOptimismService
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class VelodromeOptimismPoolingMarketProvider(
    private val velodromeOptimismService: VelodromeOptimismService
) : PoolingMarketProvider() {


    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        val pairFactoryContract = PairFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = velodromeOptimismService.getPoolFactory()
        )

        return@coroutineScope pairFactoryContract.allPairs().map {
            async {

                val poolingToken = getToken(it)
                val tokens = poolingToken.underlyingTokens

                try {
                    PoolingMarket(
                        id = "pooling-velodrome-optimism-$it",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        marketSize = getMarketSize(
                            poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                            it
                        ),
                        positionFetcher = defaultPositionFetcher(poolingToken.address),
                        address = it,
                        name = poolingToken.name,
                        breakdown = defaultBreakdown(tokens, poolingToken.address),
                        symbol = poolingToken.symbol,
                        tokens = poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                        tokenType = TokenType.VELODROME
                    )
                } catch (ex: Exception) {
                    logger.error("Error while fetching pooling market $it", ex)
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }


    override fun getProtocol(): Protocol {
        return Protocol.VELODROME
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}