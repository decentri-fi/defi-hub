package io.defitrack.protocol.bancor.pool

import io.defitrack.common.network.Network
import io.defitrack.common.utils.RefetchableValue
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.bancor.BancorEthereumProvider
import io.defitrack.protocol.bancor.contract.BancorNetworkContract
import io.defitrack.protocol.bancor.contract.BancorPoolCollection
import io.defitrack.protocol.bancor.contract.PoolTokenContract
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class BancorEthereumPoolingMarketProvider(
    private val bancorEthreumProvider: BancorEthereumProvider,
) : PoolingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.BANCOR
    }

    val bancorPoolCollection by lazy {
        runBlocking {
            BancorPoolCollection(
                getBlockchainGateway(),
                bancorEthreumProvider.bancorPoolCollection
            )
        }
    }

    val poolTokenContractAbi by lazy {
        runBlocking {
            getAbi("bancor/PoolToken.json")
        }
    }
    val bancorNetworkAbi by lazy {
        runBlocking {
            getAbi("bancor/BancorNetwork.json")
        }
    }

    val bancorNetworkContract by lazy {
        BancorNetworkContract(
            getBlockchainGateway(), bancorNetworkAbi, bancorEthreumProvider.bancorNetwork
        )
    }

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        bancorPoolCollection.allPools().map { pool ->
            async {
                try {
                    val token = getToken(pool)
                    val poolTokenContract = PoolTokenContract(
                        getBlockchainGateway(),
                        poolTokenContractAbi,
                        pool
                    )

                    val underlying = getToken(poolTokenContract.reserveToken())
                        .toFungibleToken()
                    create(
                        identifier = pool,
                        address = pool,
                        name = token.name,
                        symbol = token.symbol,
                        tokens = listOf(
                            underlying
                        ),
                        tokenType = TokenType.BANCOR,
                        investmentPreparer = BancorPoolInvestmentPreparer(
                            getERC20Resource(), bancorNetworkContract, underlying.address
                        ),
                        positionFetcher = defaultPositionFetcher(token.address),
                        totalSupply = RefetchableValue.refetchable {
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