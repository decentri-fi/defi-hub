package io.defitrack.protocol.balancer.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import io.defitrack.protocol.balancer.polygon.BalancerGaugePolygonGraphProvider
import io.defitrack.market.farming.FarmingMarketService
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.FarmingPositionFetcher
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class BalancerPolygonFarmingMarketService(
    private val balancerPolygonPoolGraphProvider: BalancerGaugePolygonGraphProvider,
    private val erC20Resource: ERC20Resource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val abiResource: ABIResource
) :
    FarmingMarketService() {

    val balancerGaugeContractAbi by lazy {
        abiResource.getABI("balancer/gauge.json")
    }

    override suspend fun fetchStakingMarkets(): List<FarmingMarket> = coroutineScope {
        balancerPolygonPoolGraphProvider.getGauges().map {
            async {
                try {
                    val stakedToken = erC20Resource.getTokenInformation(getNetwork(), it.poolAddress)
                    val gauge = BalancerGaugeContract(
                        blockchainGatewayProvider.getGateway(getNetwork()),
                        balancerGaugeContractAbi,
                        it.id
                    )

                    FarmingMarket(
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
                        balanceFetcher = FarmingPositionFetcher(
                            gauge.address,
                            { user -> gauge.balanceOfMethod(user) }
                        )
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
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