package io.defitrack.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aura.AuraBoosterContract
import io.defitrack.protocol.aura.CrvRewardsContract
import kotlinx.coroutines.*
import org.springframework.stereotype.Service

@Service
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
                        name = crvrewards.name(),
                        identifier = crvrewards.address,
                        farmType = ContractType.VAULT,
                        stakedToken = asset.toFungibleToken(),
                        rewardTokens = listOf(getToken(crvrewards.rewardToken.await()).toFungibleToken()),
                        vaultType = "aura-deposit",
                        marketSize = Refreshable.refreshable {
                            getMarketSize(asset.toFungibleToken(), crvrewards.rewardToken.await())
                        },
                        balanceFetcher = defaultPositionFetcher(crvrewards.address)
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