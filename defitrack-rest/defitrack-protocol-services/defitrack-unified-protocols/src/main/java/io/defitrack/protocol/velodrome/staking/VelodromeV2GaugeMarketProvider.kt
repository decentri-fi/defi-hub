package io.defitrack.protocol.velodrome.staking

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.claimable.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.network.toVO
import io.defitrack.protocol.Company
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.VelodromeV2GaugeContract
import io.defitrack.protocol.contract.VoterContract
import io.defitrack.protocol.velodrome.pooling.VelodromeV2OptimismPoolingMarketProvider
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.VELODROME)
@ConditionalOnProperty(value = ["velodromev2.enabled"], havingValue = "true", matchIfMissing = true)
class VelodromeV2GaugeMarketProvider(
    private val poolingMarketProvider: VelodromeV2OptimismPoolingMarketProvider
) : FarmingMarketProvider() {

    val voter = "0x41c914ee0c7e1a5edcd0295623e6dc557b5abf3c"

    val deferredVoterContract = lazyAsync {
        VoterContract(
            getBlockchainGateway(),
            voter
        )
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val voterContract = deferredVoterContract.await()
        poolingMarketProvider.getMarkets().forEach {
            val gauge = voterContract.gauges(it.address)
            throttled {
                launch {
                    if (gauge != "0x0000000000000000000000000000000000000000") {
                        try {
                            val gaugeContract = VelodromeV2GaugeContract(
                                getBlockchainGateway(),
                                gauge
                            )

                            val stakedToken = getToken(gaugeContract.stakedToken.await())
                            val rewardToken = getToken(gaugeContract.rewardToken.await())

                            val market = create(
                                name = stakedToken.name + " Gauge V2",
                                identifier = stakedToken.symbol + "-v1-${gauge}",
                                farmType = ContractType.LIQUIDITY_MINING,
                                rewardTokens = listOf(rewardToken.toFungibleToken()),
                                marketSize = refreshable {
                                    getMarketSize(
                                        stakedToken.toFungibleToken(),
                                        gaugeContract.address
                                    )
                                },
                                stakedToken = stakedToken.toFungibleToken(),
                                balanceFetcher = defaultPositionFetcher(gauge),
                                rewardsFinished = false,
                                metadata = mapOf(
                                    "address" to gauge,
                                ),
                                claimableRewardFetcher = ClaimableRewardFetcher(
                                    listOf(
                                        Reward(
                                            token = rewardToken.toFungibleToken(),
                                            contractAddress = gaugeContract.address,
                                            getRewardFunction = { user ->
                                                gaugeContract.earnedFn(user)
                                            }
                                        )
                                    ),
                                    preparedTransaction = { user ->
                                        PreparedTransaction(
                                            network = getNetwork().toVO(),
                                            function = gaugeContract.getRewardFn(user),
                                            to = gaugeContract.address,
                                            from = user
                                        )
                                    }
                                )
                            )

                            send(market)
                        } catch (ex: Exception) {
                            logger.error("Failed to fetch gauge market with pooling market {}", it.address, ex)
                        }
                    }
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.VELODROME_V2
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}