package io.defitrack.set.farming

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.set.EthereumSetProvider
import io.defitrack.protocol.set.SetTokenContract
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service

@Service
class EthereumSetPoolingMarketProvider(
    private val ethereumSetProvider: EthereumSetProvider,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    erC20Resource: ERC20Resource
) : PoolingMarketProvider(erC20Resource) {

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val gateway = blockchainGatewayProvider.getGateway(getNetwork())
        return ethereumSetProvider.getSets().mapNotNull {
            try {
                val tokenContract = SetTokenContract(
                    gateway, it
                )
                val positions = tokenContract.getPositions()
                PoolingMarket(
                    id = "set-ethereum-$it",
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
                    positionFetcher = defaultBalanceFetcher(it),
                    investmentPreparer = null
                )
            } catch (ex: Exception) {
                logger.error("Unable to import set with address $it")
                null
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.SET
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}