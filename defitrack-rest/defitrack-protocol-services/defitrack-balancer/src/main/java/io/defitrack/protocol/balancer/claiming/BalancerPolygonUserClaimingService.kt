package io.defitrack.protocol.balancer.claiming

import io.defitrack.abi.ABIResource
import io.defitrack.claimable.ClaimableElement
import io.defitrack.claimable.ClaimableService
import io.defitrack.claimable.ClaimableToken
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.BalancerPolygonService
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.util.*

@Service
class BalancerPolygonUserClaimingService(
    private val balancerPolygonService: BalancerPolygonService,
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

    override fun claimables(address: String): List<ClaimableElement> = runBlocking {
        val markets = balancerPolygonService.getGauges()
        markets.map { gauge ->
            async {
                println("gauge ${gauge.id}")
                BalancerGaugeContract(
                    contractAccessorGateway.getGateway(getNetwork()),
                    gaugeContractAbi,
                    gauge.id
                ).getBalances(address)
                    .filter { it.balance > BigInteger.ZERO }
                    .map { balanceResult ->
                        val token = erC20Resource.getTokenInformation(getNetwork(), balanceResult.token)
                        ClaimableElement(
                            id = UUID.randomUUID().toString(),
                            name = token.name + " reward",
                            address = gauge.id,
                            type = "balancer-reward",
                            protocol = getProtocol(),
                            network = getNetwork(),
                            claimableToken = ClaimableToken(
                                name = token.name,
                                symbol = token.symbol,
                                amount = balanceResult.balance.asEth(token.decimals).toDouble()
                            )
                        )
                    }
            }
        }.flatMap {
            it.await()
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}
