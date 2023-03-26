package io.defitrack.staking

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aura.AuraBoosterContract
import io.defitrack.protocol.aura.CrvRewardsContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class AuraDepositVaultFarmingMarketProvider(
) : FarmingMarketProvider() {

    val booster by lazy {
        runBlocking {
            AuraBoosterContract(
                getBlockchainGateway()
            )
        }
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        booster.poolInfos().map {
            async {
                try {
                    val crvrewards = CrvRewardsContract(getBlockchainGateway(), it.crvRewards)
                    val asset = getToken(crvrewards.asset())

                    create(
                        name = crvrewards.name(),
                        identifier = crvrewards.address,
                        farmType = ContractType.VAULT,
                        stakedToken = asset.toFungibleToken(),
                        rewardTokens = listOf(getToken(crvrewards.rewardToken()).toFungibleToken()),
                        vaultType = "aura-deposit",
                        marketSize = getMarketSize(asset.toFungibleToken(), crvrewards.rewardToken()),
                        balanceFetcher = defaultPositionFetcher(crvrewards.address)
                    )
                } catch (ex: Exception) {
                    logger.error("Error fetching market for ${it.crvRewards}", ex)
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}