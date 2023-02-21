package io.defitrack.protocol.balancer.claiming

import io.defitrack.abi.ABIResource
import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableRewardProvider
import io.defitrack.claimable.PrepareClaimCommand
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import io.defitrack.protocol.balancer.staking.BalancerArbitrumFarmingMarketProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.util.*

@Service
class BalancerArbitrumUserClaimingRewardProvider(
    private val marketProvider: BalancerArbitrumFarmingMarketProvider,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    abiResource: ABIResource
) : ClaimableRewardProvider() {

    val gaugeContractAbi by lazy {
        runBlocking {
            abiResource.getABI("balancer/gauge.json")
        }
    }

    override suspend fun claimables(address: String): List<Claimable> =
        coroutineScope {
            marketProvider.getMarkets().map { liquidityGauge ->
                async {
                    return@async try {
                        val gaugeContract = BalancerGaugeContract(
                            blockchainGatewayProvider.getGateway(getNetwork()),
                            gaugeContractAbi,
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

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}
