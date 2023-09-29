package io.defitrack.protocol.bancor.pool

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.bancor.BancorEthereumProvider
import io.defitrack.protocol.bancor.contract.BancorNetworkContract
import io.defitrack.protocol.bancor.contract.BancorPoolCollectionContract
import io.defitrack.protocol.bancor.contract.PoolTokenContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class BancorEthereumPoolingMarketProvider(
    private val bancorEthreumProvider: BancorEthereumProvider,
) : PoolingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.BANCOR
    }

    val bancorPoolCollectionContract = lazyAsync {
        BancorPoolCollectionContract(
            getBlockchainGateway(),
            bancorEthreumProvider.bancorPoolCollection
        )
    }

    val bancorNetworkContract = lazyAsync {
        BancorNetworkContract(
            getBlockchainGateway(), bancorEthreumProvider.bancorNetwork
        )
    }

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        bancorPoolCollectionContract.await().allPools().map { pool ->
            async {
                try {
                    val token = getToken(pool)
                    val poolTokenContract = PoolTokenContract(
                        getBlockchainGateway(),
                        pool
                    )

                    val underlying = getToken(poolTokenContract.reserveToken())
                        .toFungibleToken()
                    create(
                        identifier = pool,
                        address = pool,
                        name = token.name,
                        symbol = token.symbol,
                        tokens = listOf(underlying),
                        investmentPreparer = BancorPoolInvestmentPreparer(
                            getERC20Resource(), bancorNetworkContract.await(), underlying.address
                        ),
                        positionFetcher = defaultPositionFetcher(token.address),
                        totalSupply = refreshable {
                            getToken(pool).totalDecimalSupply()
                        }
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}