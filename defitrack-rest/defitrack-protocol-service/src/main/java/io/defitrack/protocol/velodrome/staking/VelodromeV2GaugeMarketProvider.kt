package io.defitrack.protocol.velodrome.staking

import arrow.core.Either
import arrow.core.None
import arrow.core.some
import arrow.fx.coroutines.parMap
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.velodrome.contract.VelodromeV2GaugeContract
import io.defitrack.protocol.velodrome.contract.VoterContract
import io.defitrack.protocol.velodrome.pooling.VelodromeV2OptimismPoolingMarketProvider
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
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
        poolingMarketProvider.getMarkets().parMap(concurrency = 12) {
            Either.catch {
                val gauge = voterContract.gauges(it.address)
                if (gauge != "0x0000000000000000000000000000000000000000") {
                    val gaugeContract = VelodromeV2GaugeContract(
                        getBlockchainGateway(),
                        gauge
                    )

                    val stakedToken = getToken(gaugeContract.stakedToken.await())
                    val rewardToken = getToken(gaugeContract.rewardToken.await())

                    create(
                        name = stakedToken.name + " Gauge V2",
                        identifier = stakedToken.symbol + "-v1-${gauge}",
                        rewardTokens = listOf(rewardToken),
                        marketSize = refreshable {
                            getMarketSize(
                                stakedToken,
                                gaugeContract.address
                            )
                        },
                        stakedToken = stakedToken,
                        positionFetcher = defaultPositionFetcher(gauge),
                        deprecated = false,
                        metadata = mapOf(
                            "address" to gauge,
                        ),
                        claimableRewardFetcher = ClaimableRewardFetcher(
                            Reward(
                                token = rewardToken,
                                getRewardFunction = gaugeContract::earnedFn
                            ),
                            preparedTransaction = selfExecutingTransaction(gaugeContract::getRewardFn)
                        )
                    ).some()
                } else {
                    None
                }
            }
        }.mapNotNull {
            it.mapLeft {
                logger.error("Error while fetching gauge: {}", it.message)
            }.getOrNull()
        }.forEach {
            it.onSome { send(it) }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.VELODROME_V2
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}