package io.defitrack.protocol.bancor.pool

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.bancor.BancorEthereumGraphProvider
import io.defitrack.protocol.bancor.contract.BancorNetworkContract
import io.defitrack.protocol.bancor.contract.PoolTokenContract
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class BancorEthereumPoolingMarketProvider(
    erC20Resource: ERC20Resource,
    private val abiResource: ABIResource,
    private val bancorEthereumGraphProvider: BancorEthereumGraphProvider,
    gatewayProvider: BlockchainGatewayProvider
) : PoolingMarketProvider(erC20Resource) {

    val gateway = gatewayProvider.getGateway(getNetwork())

    val poolTokenContractAbi by lazy {
        abiResource.getABI("bancor/PoolToken.json")
    }
    val bancorNetworkAbi by lazy {
        abiResource.getABI("bancor/BancorNetwork.json")
    }

    val bancorNetworkContract by lazy {
        BancorNetworkContract(
            gateway, bancorNetworkAbi, bancorEthereumGraphProvider.bancorNetwork()
        )
    }

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        bancorEthereumGraphProvider.getLiquidityPools().map {
            async {
                try {
                    val token = getToken(it.id)
                    val poolTokenContract = PoolTokenContract(
                        gateway,
                        poolTokenContractAbi,
                        it.id
                    )

                    val underlying = getToken(poolTokenContract.reserveToken())
                        .toFungibleToken()
                    PoolingMarket(
                        id = "bancor-ethereum-${it.id}",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        address = it.id,
                        name = token.name,
                        symbol = token.symbol,
                        tokens = listOf(
                            underlying
                        ),
                        tokenType = TokenType.BANCOR,
                        marketSize = it.totalValueLockedUSD,
                        investmentPreparer = BancorPoolInvestmentPreparer(
                            erc20Resource, bancorNetworkContract, underlying.address
                        ),
                        positionFetcher = defaultBalanceFetcher(token.address)
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.BANCOR
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}