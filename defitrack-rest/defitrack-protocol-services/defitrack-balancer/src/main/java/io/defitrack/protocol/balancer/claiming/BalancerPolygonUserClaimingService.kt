package io.defitrack.protocol.balancer.claiming

import io.defitrack.abi.ABIResource
import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableService
import io.defitrack.claimable.PrepareClaimCommand
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.BalancerPolygonService
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import io.defitrack.protocol.balancer.staking.BalancerPolygonStakingMarketService
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.util.*

@Service
class BalancerPolygonUserClaimingService(
    private val balancerPolygonStakingMarketService: BalancerPolygonStakingMarketService,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val erC20Resource: ERC20Resource,
    abiResource: ABIResource
) : ClaimableService {

    val gaugeContractAbi by lazy {
        abiResource.getABI("balancer/gauge.json")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun claimables(address: String): List<Claimable> =
        withContext(Dispatchers.IO.limitedParallelism(10)) {
            balancerPolygonStakingMarketService.getStakingMarkets().map { liquidityGauge ->
                val gaugeContract = BalancerGaugeContract(
                    contractAccessorGateway.getGateway(getNetwork()),
                    gaugeContractAbi,
                    liquidityGauge.id
                )
                gaugeContract.getBalances(address)
                    .filter { it.balance > BigInteger.ZERO }
                    .map { balanceResult ->
                        async {
                            try {
                                val token = erC20Resource.getTokenInformation(getNetwork(), balanceResult.token)
                                Claimable(
                                    id = UUID.randomUUID().toString(),
                                    name = token.name + " reward",
                                    address = liquidityGauge.id,
                                    type = "balancer-reward",
                                    protocol = getProtocol(),
                                    network = getNetwork(),
                                    claimableToken = token.toFungibleToken(),
                                    amount = balanceResult.balance,
                                    claimTransaction = BalancerClaimPreparer(
                                        gaugeContract
                                    ).prepare(PrepareClaimCommand(user = address))
                                )
                            } catch (ex: Exception) {
                                null
                            }
                        }
                    }
            }.flatMap {
                it.awaitAll()
            }.filterNotNull()
        }

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}
