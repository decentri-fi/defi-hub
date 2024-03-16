package io.defitrack.protocol.application.aerodrome.farming

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.fx.coroutines.parMap
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.application.aerodrome.pooling.AerodromePoolingMarketProvider
import io.defitrack.protocol.velodrome.contract.VelodromeV2GaugeContract
import io.defitrack.protocol.velodrome.contract.VoterContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AERODROME)
class AerodromeGaugeMarketProvider(
    private val poolingMarketProvider: AerodromePoolingMarketProvider
) : FarmingMarketProvider() {

    val voter = "0x16613524e02ad97edfef371bc883f2f5d6c480a5"

    val voterContract = lazyAsync {
        with(getBlockchainGateway()) {
            VoterContract(
                voter
            )
        }
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        poolingMarketProvider.getMarkets().parMap(concurrency = 12) { poolingMarket ->
            Either.catch {
                createGauge(poolingMarket)
            }
        }.mapNotNull {
            it.mapLeft { throwable ->
                logger.error("Error creating gauge market: {}", throwable.message)
            }.getOrNull()
        }.forEach {
            it.onSome {
                send(it)
            }
        }
    }

    private suspend fun AerodromeGaugeMarketProvider.createGauge(it: PoolingMarket): Option<FarmingMarket> {
        val gauge = voterContract.await().gauges(it.address)

        return if (gauge != "0x0000000000000000000000000000000000000000") {
            val contract = with(getBlockchainGateway()) {
                VelodromeV2GaugeContract(gauge)
            }

            val stakedToken = getToken(contract.stakedToken.await())
            val rewardToken = getToken(contract.rewardToken.await())

            create(
                name = stakedToken.name + " Gauge V2",
                identifier = stakedToken.symbol + "-v1-${gauge}",
                rewardToken = rewardToken,
                marketSize = refreshable {
                    getMarketSize(
                        stakedToken,
                        contract.address
                    )
                },
                stakedToken = stakedToken,
                positionFetcher = defaultPositionFetcher(gauge),
                deprecated = false,
                internalMetadata = mapOf(
                    "address" to gauge,
                    "contract" to contract
                ),
                type = "aerodrome.gauge",
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = rewardToken,
                        getRewardFunction = contract::earnedFn
                    ),
                    preparedTransaction = selfExecutingTransaction(contract::getRewardFn)
                )
            ).some()
        } else {
            None
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.AERODROME
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}