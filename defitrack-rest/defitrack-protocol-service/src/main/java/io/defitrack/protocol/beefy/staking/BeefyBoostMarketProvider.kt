package io.defitrack.protocol.beefy.staking

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.BeefyBoostService
import io.defitrack.protocol.beefy.contract.`BeefyLaunchPoolContract`
import io.defitrack.protocol.beefy.domain.BeefyLaunchPool
import io.defitrack.protocol.maple.Market
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

abstract class BeefyBoostMarketProvider(
    private val beefyFarmingMarketProvider: BeefyFarmingMarketProvider,
) : FarmingMarketProvider() {


    @Autowired
    private lateinit var beefyBoostService: BeefyBoostService

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        beefyBoostService.getBoosts(getNetwork())
            .parMapNotNull(concurrency = 8) {
                catch {
                    val contract = BeefyLaunchPoolContract(getBlockchainGateway(), it.earnContractAddress)

                    /*     val underlyingMarket = beefyFarmingMarketProvider.getMarkets().find {
                             it.metadata["vaultAddress"]?.toString()?.lowercase() == contract.stakedToken.await().lowercase()
                         } */

                    val want = getToken(contract.stakedToken.await()) //should be fetched from farms
                    val reward = getToken(it.earnedTokenAddress)

                    create(
                        name = want.name + " Boost",
                        identifier = it.id,
                        stakedToken = want,
                        rewardToken = reward,
                        deprecated = it.status == "eol" || Date(it.periodFinish).before(Date()),
                        metadata = mapOf("type" to "boost"),
                        positionFetcher = defaultPositionFetcher(contract.address),
                        claimableRewardFetcher = ClaimableRewardFetcher(
                            Reward(
                                reward,
                                contract::earned
                            ),
                            preparedTransaction = selfExecutingTransaction(contract::getRewardfn)
                        ),
                        exitPositionPreparer = prepareExit {
                            contract.exitPosition(it.amount)
                        }
                    )
                }.mapLeft {
                    logger.info("Unable to create beefy boost market: {}", it.message)
                }.getOrNull()
            }.forEach {
                send(it)
            }
    }

    override fun getProtocol(): Protocol {
        return Protocol.BEEFY
    }

    override fun order(): Int {
        return 2
    }
}