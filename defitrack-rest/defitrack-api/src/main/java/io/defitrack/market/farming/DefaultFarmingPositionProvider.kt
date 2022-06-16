package io.defitrack.market.farming

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.token.ERC20Resource
import java.math.BigInteger

abstract class DefaultFarmingPositionProvider(
    erC20Resource: ERC20Resource,
    val farmingMarketService: FarmingMarketService,
    val gateway: BlockchainGatewayProvider
) : FarmingPositionProvider(erC20Resource) {
    override suspend fun getStakings(address: String): List<FarmingPosition> {
        val markets = farmingMarketService.getStakingMarkets().filter {
            it.balanceFetcher != null
        }

        return gateway.getGateway(getNetwork()).readMultiCall(
            markets.map {
                it.balanceFetcher!!.toMulticall(address)
            }
        ).mapIndexed { index, retVal ->
            val market = markets[index]
            val balance = market.balanceFetcher!!.extractBalance(retVal)

            if (balance > BigInteger.ONE) {

                val underlyingBalance = market.underlyingBalanceFetcher?.let {
                    try {
                        val underlyingRetVal =
                            gateway.getGateway(getNetwork()).executeCall(it.address, it.function(address))
                        it.extractBalance(underlyingRetVal)
                    } catch (ex: Exception) {
                        null
                    }
                }

                FarmingPosition(
                    market,
                    balance,
                    underlyingBalance
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return farmingMarketService.getProtocol()
    }

    override fun getNetwork(): Network {
        return farmingMarketService.getNetwork()
    }
}