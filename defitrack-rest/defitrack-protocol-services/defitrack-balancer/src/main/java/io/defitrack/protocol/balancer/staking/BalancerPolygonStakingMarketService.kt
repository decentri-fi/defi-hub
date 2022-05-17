package io.defitrack.protocol.balancer.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.BalancerPolygonService
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarketBalanceFetcher
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import org.springframework.stereotype.Component

@Component
class BalancerPolygonStakingMarketService(
    private val balancerPolygonService: BalancerPolygonService,
    private val erC20Resource: ERC20Resource,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val abiResource: ABIResource
) :
    StakingMarketService() {

    val balancerGaugeContractAbi by lazy {
        abiResource.getABI("balancer/gauge.json")
    }

    override suspend fun fetchStakingMarkets(): List<StakingMarketElement> {
        return try {
            balancerPolygonService.getGauges().map {
                val stakedToken = erC20Resource.getTokenInformation(getNetwork(), it.poolAddress)
                val gauge = BalancerGaugeContract(
                    contractAccessorGateway.getGateway(getNetwork()),
                    balancerGaugeContractAbi,
                    it.id
                )

                StakingMarketElement(
                    id = "bal-${it.id}",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = stakedToken.symbol + " Gauge",
                    stakedToken = stakedToken.toFungibleToken(),
                    rewardTokens = getRewardTokens(
                        gauge
                    ).map { reward ->
                        reward.toFungibleToken()
                    },
                    contractAddress = it.id,
                    vaultType = "balancerGauge",
                    balanceFetcher = StakingMarketBalanceFetcher(
                        gauge.address,
                        {user -> gauge.balanceOfMethod(user)}
                    )
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            emptyList()
        }
    }

    fun getRewardTokens(balancerGaugeContract: BalancerGaugeContract): List<TokenInformation> {
        return (0..3).mapNotNull {
            try {
                val rewardToken = balancerGaugeContract.getRewardToken(it)
                if (rewardToken != "0x0000000000000000000000000000000000000000") {
                    erC20Resource.getTokenInformation(getNetwork(), rewardToken)
                } else {
                    null
                }
            } catch (ex: Exception) {
                null
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