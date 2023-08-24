package io.defitrack.protocol.claim

import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableRewardProvider
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.HopStakingRewardContract
import io.defitrack.protocol.farming.HopArbitrumFarmingMarketProvider
import io.defitrack.protocol.farming.HopOptimismFarmingMarketProvider
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.util.*

@Component
class HopOptimismClaimableRewardProvider(
    private val marketProvider: HopOptimismFarmingMarketProvider,
) : ClaimableRewardProvider() {

    override suspend fun claimables(address: String): List<Claimable> {
        val markets = marketProvider.getMarkets()

        return marketProvider.getBlockchainGateway().readMultiCall(
            markets.map {
                val contract = it.metadata["contract"] as HopStakingRewardContract

                MultiCallElement(
                    contract.earnedFn(address),
                    contract.address
                )
            }
        ).mapIndexed { index, results ->
            val result = results[0].value as BigInteger
            if (result > BigInteger.ZERO) {
                val market = markets[index]
                val contract = market.metadata["contract"] as HopStakingRewardContract

                Claimable(
                    id = UUID.randomUUID().toString(),
                    name = market.name + " reward",
                    type = "hop-reward",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    claimableTokens = market.rewardTokens,
                    amount = result,
                    claimTransaction = PreparedTransaction(
                        marketProvider.getNetwork().toVO(),
                        contract.getRewardFn(),
                        contract.address,
                        address
                    )
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return marketProvider.getProtocol()
    }

    override fun getNetwork(): Network {
        return marketProvider.getNetwork()
    }
}