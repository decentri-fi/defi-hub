package io.defitrack.protocol.aura.staking

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aura.AuraBoosterContract
import io.defitrack.protocol.aura.CrvRewardsContract
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AURA)
class AuraDepositVaultFarmingMarketProvider : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val booster = AuraBoosterContract(
            getBlockchainGateway()
        )
        booster.poolInfos().parMapNotNull(concurrency = 12) {
            try {
                val crvrewards = crvRewardsContract(it.crvRewards)
                val asset = getToken(crvrewards.asset())

                val crvRewardsToken = getToken(it.crvRewards)

                create(
                    name = crvRewardsToken.name,
                    identifier = crvrewards.address,
                    stakedToken = asset,
                    rewardToken = getToken(crvrewards.rewardToken.await()),
                    marketSize = refreshable {
                        getMarketSize(asset, crvrewards.rewardToken.await())
                    },
                    positionFetcher = defaultPositionFetcher(crvrewards.address)
                )
            } catch (ex: Exception) {
                logger.error("Error fetching market for ${it.crvRewards}", ex)
                null
            }
        }
    }

    private suspend fun crvRewardsContract(crvRewards: String): CrvRewardsContract =
        with(getBlockchainGateway()) { CrvRewardsContract(crvRewards) }

    override fun getProtocol(): Protocol {
        return Protocol.AURA
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}