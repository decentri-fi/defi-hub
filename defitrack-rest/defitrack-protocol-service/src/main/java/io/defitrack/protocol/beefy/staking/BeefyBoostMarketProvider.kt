package io.defitrack.protocol.beefy.staking

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.contract.`BeefyLaunchPoolContract`
import io.defitrack.protocol.beefy.domain.BeefyLaunchPool
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

abstract class BeefyBoostMarketProvider(
    private val launchpools: MutableList<BeefyLaunchPool>
) : FarmingMarketProvider() {

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        launchpools.map {

            val contract = BeefyLaunchPoolContract(getBlockchainGateway(), it.earnContractAddress)
            val want = getToken(contract.stakedToken.await()) //should be fetched from farms
            val reward = getToken(it.earnedTokenAddress)


            create(
                name = want.name + " Boost",
                identifier = it.id,
                stakedToken = want,
                rewardToken = reward,
                rewardsFinished = it.status == "eol",
                metadata = mapOf("type" to "boost")
            )
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