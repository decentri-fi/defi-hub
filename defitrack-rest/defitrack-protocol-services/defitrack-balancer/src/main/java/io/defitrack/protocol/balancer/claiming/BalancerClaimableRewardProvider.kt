package io.defitrack.protocol.balancer.claiming

import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableRewardProvider
import io.defitrack.claimable.PrepareClaimCommand
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.math.BigInteger
import java.util.*

abstract class BalancerClaimableRewardProvider(
    private val farmingMarketProvider: FarmingMarketProvider
) : ClaimableRewardProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }

    override suspend fun claimables(address: String): List<Claimable> =
        coroutineScope {
            farmingMarketProvider.getMarkets().map { liquidityGauge ->
                async {
                    return@async try {
                        val gaugeContract = BalancerGaugeContract(
                            getBlockchainGateway(),
                            liquidityGauge.metadata["address"] as String
                        )

                        val claimTransaction = BalancerClaimPreparer(
                            gaugeContract, from = address
                        ).prepare(PrepareClaimCommand(user = address))

                        gaugeContract.getBalances(address, liquidityGauge.rewardTokens)
                            .filter { it.balance > BigInteger.ZERO }
                            .map { balanceResult ->
                                Claimable(
                                    id = UUID.randomUUID().toString(),
                                    name = balanceResult.token.name + " reward",
                                    type = "balancer-reward",
                                    protocol = getProtocol(),
                                    network = getNetwork(),
                                    claimableTokens = listOf(balanceResult.token),
                                    amount = balanceResult.balance,
                                    claimTransaction = claimTransaction
                                )
                            }
                    } catch (ex: Exception) {
                        emptyList()
                    }
                }
            }.awaitAll().flatten()
        }

}