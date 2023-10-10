package io.defitrack.protocol.aerodrome.farming

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.claimable.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.network.toVO
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aerodrome.pooling.AerodromePoolingMarketProvider
import io.defitrack.protocol.velodrome.contract.VelodromeV2GaugeContract
import io.defitrack.protocol.velodrome.contract.VoterContract
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AERODROME)
class AerodromeGaugeMarketProvider(
    private val poolingMarketProvider: AerodromePoolingMarketProvider
) : FarmingMarketProvider() {

    val voter = "0x16613524e02ad97edfef371bc883f2f5d6c480a5"

    val voterContract = lazyAsync {
        VoterContract(
            getBlockchainGateway(),
            voter
        )
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        poolingMarketProvider.getMarkets().forEach {
            val gauge = voterContract.await().gauges(it.address)

            launch {
                throttled {
                    if (gauge != "0x0000000000000000000000000000000000000000") {
                        try {
                            val contract = VelodromeV2GaugeContract(
                                getBlockchainGateway(),
                                gauge
                            )

                            val stakedToken = getToken(contract.stakedToken.await())
                            val rewardToken = getToken(contract.rewardToken.await())

                            val market = create(
                                name = stakedToken.name + " Gauge V2",
                                identifier = stakedToken.symbol + "-v1-${gauge}",
                                rewardTokens = listOf(rewardToken.toFungibleToken()),
                                marketSize = Refreshable.refreshable {
                                    getMarketSize(
                                        stakedToken.toFungibleToken(),
                                        contract.address
                                    )
                                },
                                stakedToken = stakedToken.toFungibleToken(),
                                balanceFetcher = defaultPositionFetcher(gauge),
                                rewardsFinished = false,
                                metadata = mapOf(
                                    "address" to gauge,
                                    "contract" to contract
                                ),
                                claimableRewardFetcher = ClaimableRewardFetcher(
                                    Reward(
                                        token = rewardToken.toFungibleToken(),
                                        contractAddress = contract.address,
                                        getRewardFunction = { user ->
                                            contract.earnedFn(user)
                                        }
                                    ),
                                    preparedTransaction = { user ->
                                        PreparedTransaction(
                                            network = getNetwork().toVO(),
                                            function = contract.getRewardFn(user),
                                            to = contract.address,
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
        return Protocol.AERODROME
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}