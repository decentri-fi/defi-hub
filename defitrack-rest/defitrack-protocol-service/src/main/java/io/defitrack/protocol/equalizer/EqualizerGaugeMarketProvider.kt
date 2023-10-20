package io.defitrack.protocol.equalizer

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.EQUALIZER)
class EqualizerGaugeMarketProvider(
    private val equalizerPoolingMarketProvider: EqualizerPoolingMarketProvider,
    private val equalizerService: EqualizerService
) : FarmingMarketProvider() {

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {

        val poolToGauges = equalizerService.gauges.await()
        val markets = equalizerPoolingMarketProvider.getMarkets()
        markets.mapNotNull { poolingMarket ->
            Either.catch {
                val gauge = poolToGauges[poolingMarket.address.lowercase()]
                if (gauge == null || gauge.isNone()) {
                    throw IllegalArgumentException("Gauge not found for ${poolingMarket.address}")
                }

                val gaugeContract = EqualizerGaugeContract(getBlockchainGateway(), gauge.getOrNull()!!)

                val rewards = gaugeContract.getRewards().map { getToken(it) }
                val stake = getToken(gaugeContract.stake.await())

                create(
                    name = poolingMarket.name + " Gauge",
                    identifier = gaugeContract.address,
                    rewardTokens = rewards,
                    stakedToken = stake,
                    claimableRewardFetcher = ClaimableRewardFetcher(
                        rewards.map {
                            Reward(
                                it,
                                gaugeContract.address,
                                gaugeContract.earnedFnFor(it.address),
                            )
                        },
                        preparedTransaction = selfExecutingTransaction(gaugeContract::getRewardFn)
                    )
                )
            }.mapLeft {
                logger.error("Error while creating farming market {}: {} ", poolingMarket.address, it.message)
            }.getOrNull()
        }.forEach {
            send(it)
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.EQUALIZER
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}