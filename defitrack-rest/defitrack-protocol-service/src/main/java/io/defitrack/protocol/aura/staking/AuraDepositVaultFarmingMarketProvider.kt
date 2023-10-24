package io.defitrack.protocol.aura.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aura.AuraBoosterContract
import io.defitrack.protocol.aura.CrvRewardsContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AURA)
class AuraDepositVaultFarmingMarketProvider(
) : FarmingMarketProvider() {

    val booster = lazyAsync {
        AuraBoosterContract(
            getBlockchainGateway()
        )
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        booster.await().poolInfos().map {
            async {
                try {
                    val crvrewards = crvRewardsContract(it.crvRewards)
                    val asset = getToken(crvrewards.asset())

                    create(
                        name = crvrewards.readName(),
                        identifier = crvrewards.address,
                        stakedToken = asset.toFungibleToken(),
                        rewardTokens = listOf(getToken(crvrewards.rewardToken.await()).toFungibleToken()),
                        marketSize = Refreshable.refreshable {
                            getMarketSize(asset.toFungibleToken(), crvrewards.rewardToken.await())
                        },
                        positionFetcher = defaultPositionFetcher(crvrewards.address)
                    )
                } catch (ex: Exception) {
                    logger.error("Error fetching market for ${it.crvRewards}", ex)
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun crvRewardsContract(crvRewards: String): CrvRewardsContract {
        return CrvRewardsContract(getBlockchainGateway(), crvRewards)
    }

    override fun getProtocol(): Protocol {
        return Protocol.AURA
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}