package io.defitrack.protocol.bancor.pool

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.bancor.domain.BancorEthereumGraphProvider
import io.defitrack.protocol.bancor.domain.PoolTokenContract
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class BancorEthereumPoolingMarketProvider(
    private val erC20Resource: ERC20Resource,
    private val abiResource: ABIResource,
    private val bancorEthereumGraphProvider: BancorEthereumGraphProvider,
    gatewayProvider: BlockchainGatewayProvider
) : PoolingMarketService() {

    val gateway = gatewayProvider.getGateway(getNetwork())

    val poolTokenContractAbi by lazy {
        abiResource.getABI("bancor/PoolToken.json")
    }

    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> = coroutineScope {
        bancorEthereumGraphProvider.getLiquidityPools().map {
            async {
                try {
                    val token = erC20Resource.getTokenInformation(getNetwork(), it.id)
                    val poolTokenContract = PoolTokenContract(
                        gateway,
                        poolTokenContractAbi,
                        it.id
                    )


                    PoolingMarketElement(
                        id = "bancor-ethereum-${it.id}",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        address = it.id,
                        name = token.name,
                        symbol = token.symbol,
                        tokens = listOf(
                            erC20Resource.getTokenInformation(getNetwork(), poolTokenContract.reserveToken)
                                .toFungibleToken()
                        ),
                        tokenType = TokenType.BANCOR,
                        marketSize = it.totalValueLockedUSD
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