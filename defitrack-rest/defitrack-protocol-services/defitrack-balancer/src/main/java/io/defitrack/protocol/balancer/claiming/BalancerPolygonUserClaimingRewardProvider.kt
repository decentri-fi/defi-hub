package io.defitrack.protocol.balancer.claiming

import io.defitrack.abi.ABIResource
import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableRewardProvider
import io.defitrack.claimable.PrepareClaimCommand
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import io.defitrack.protocol.balancer.staking.BalancerPolygonFarmingMarketProvider
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.util.*

@Service
class BalancerPolygonUserClaimingRewardProvider(
    private val balancerPolygonStakingMarketService: BalancerPolygonFarmingMarketProvider,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val erC20Resource: ERC20Resource,
    abiResource: ABIResource
) : ClaimableRewardProvider {

    val gaugeContractAbi by lazy {
        abiResource.getABI("balancer/gauge.json")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun claimables(address: String): List<Claimable> =
        coroutineScope {
            balancerPolygonStakingMarketService.getMarkets().flatMap { liquidityGauge ->
                try {
                    val gaugeContract = BalancerGaugeContract(
                        blockchainGatewayProvider.getGateway(getNetwork()),
                        gaugeContractAbi,
                        liquidityGauge.id
                    )

                    val claimTransaction = BalancerClaimPreparer(
                        gaugeContract
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
        }

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}
