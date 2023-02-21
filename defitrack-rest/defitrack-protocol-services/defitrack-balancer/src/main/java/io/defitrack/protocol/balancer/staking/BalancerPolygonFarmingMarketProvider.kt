package io.defitrack.protocol.balancer.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import io.defitrack.protocol.balancer.polygon.BalancerGaugePolygonGraphProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class BalancerPolygonFarmingMarketProvider(
    private val balancerPolygonPoolGraphProvider: BalancerGaugePolygonGraphProvider,
    private val abiResource: ABIResource
) :
    FarmingMarketProvider() {

    val balancerGaugeContractAbi by lazy {
        runBlocking {
            abiResource.getABI("balancer/gauge.json")
        }
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        balancerPolygonPoolGraphProvider.getGauges().map {
            async {
                try {
                    val stakedToken = getToken(it.poolAddress)
                    val gauge = BalancerGaugeContract(
                        getBlockchainGateway(),
                        balancerGaugeContractAbi,
                        it.id
                    )

                    create(
                        identifier = it.id,
                        name = stakedToken.symbol + " Gauge",
                        stakedToken = stakedToken.toFungibleToken(),
                        rewardTokens = getRewardTokens(
                            gauge
                        ).map { reward ->
                            reward.toFungibleToken()
                        },
                        vaultType = "balancerGauge",
                        balanceFetcher = PositionFetcher(
                            gauge.address,
                            { user -> gauge.balanceOfMethod(user) }
                        ),
                        farmType = FarmType.STAKING,
                        metadata = mapOf("address" to it.id)
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun getRewardTokens(balancerGaugeContract: BalancerGaugeContract): List<TokenInformationVO> {
        return (0..3).mapNotNull {
            try {
                val rewardToken = balancerGaugeContract.getRewardToken(it)
                if (rewardToken != "0x0000000000000000000000000000000000000000") {
                    getToken(rewardToken)
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