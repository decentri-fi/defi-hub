package io.defitrack.claimable

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.protocol.Protocol
import java.math.BigInteger

abstract class DefaultClaimableRewardProvider(
    private val farmingMarketProvider: FarmingMarketProvider,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : ClaimableRewardProvider {

    override suspend fun claimables(address: String): List<Claimable> {

        val markets = farmingMarketProvider.getMarkets().filter {
            it.claimableRewardFetcher != null
        }

        if (markets.isEmpty()) {
            return emptyList()
        }

        return blockchainGatewayProvider.getGateway(getNetwork()).readMultiCall(
            markets.map {
                it.claimableRewardFetcher!!.toMulticall(address)
            }
        ).mapIndexed { index, retVal ->
            val market = markets[index]
            val earned = market.claimableRewardFetcher!!.extract(retVal)

            if (earned > BigInteger.ONE) {
                Claimable(
                    id = "rwrd_${market.id}",
                    type = market.contractType,
                    name = market.name,
                    protocol = getProtocol(),
                    network = getNetwork(),
                    amount = earned,
                    claimableTokens = market.rewardTokens,
                    claimTransaction = market.claimableRewardFetcher.preparedTransaction(address),
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return farmingMarketProvider.getProtocol()
    }

    override fun getNetwork(): Network {
        return farmingMarketProvider.getNetwork()
    }
}