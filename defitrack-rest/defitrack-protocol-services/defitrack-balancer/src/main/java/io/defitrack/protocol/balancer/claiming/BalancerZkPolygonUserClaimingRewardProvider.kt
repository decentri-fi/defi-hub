package io.defitrack.protocol.balancer.claiming

import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableRewardProvider
import io.defitrack.claimable.PrepareClaimCommand
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerGaugeZkEvmContract
import io.defitrack.protocol.balancer.staking.BalancerPolygonZkEvmGaugeMarketProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.util.*

@Component
class BalancerZkPolygonUserClaimingRewardProvider(
    private val marketProvider: BalancerPolygonZkEvmGaugeMarketProvider,
) : ClaimableRewardProvider() {
    override suspend fun claimables(address: String): List<Claimable> {
        return coroutineScope {
            marketProvider.getMarkets().map { liquidityGauge ->
                async {
                    return@async try {
                        val gaugeContract = BalancerGaugeZkEvmContract(
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

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }

    override fun getNetwork(): Network {
        return Network.POLYGON_ZKEVM
    }
}
