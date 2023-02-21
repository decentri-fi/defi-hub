package io.defitrack.set.farming

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.set.EthereumSetProvider
import io.defitrack.protocol.set.PolygonSetProvider
import io.defitrack.protocol.set.SetTokenContract
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class PolygonSetPoolingMarketProvider(
    private val polygonSetProvider: PolygonSetProvider,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope{
        return@coroutineScope polygonSetProvider.getSetTokens().mapNotNull {
            async {
                try {
                    val tokenContract = SetTokenContract(
                        getBlockchainGateway(), it
                    )
                    val positions = tokenContract.getPositions()
                    PoolingMarket(
                        id = "set-polygon-$it",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        address = it,
                        name = tokenContract.name(),
                        symbol = tokenContract.symbol(),
                        tokens = positions.map {
                            getToken(it.token).toFungibleToken()
                        },
                        apr = null,
                        marketSize = null,
                        tokenType = TokenType.SET,
                        positionFetcher = defaultPositionFetcher(it),
                        investmentPreparer = null
                    )
                } catch (ex: Exception) {
                    logger.error("Unable to import set with address $it", ex)
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SET
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}