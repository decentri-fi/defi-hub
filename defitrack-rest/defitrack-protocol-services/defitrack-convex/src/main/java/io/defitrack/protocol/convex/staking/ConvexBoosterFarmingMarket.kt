package io.defitrack.protocol.convex.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexEthereumService
import io.defitrack.protocol.convex.contract.ConvexBoosterContract
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class ConvexBoosterFarmingMarket(
    convexService: ConvexEthereumService,
) : FarmingMarketProvider() {

    val booster = lazyAsync {
        ConvexBoosterContract(
            getBlockchainGateway(),
            convexService.provideBooster()
        )
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = booster.await()
        return coroutineScope {
            val poolInfos = contract.poolInfos()

            List(poolInfos.size) { idx ->
                val stakedToken = getToken(poolInfos[idx].lpToken)
                create(
                    identifier = contract.address + "-" + idx,
                    name = "Convex Crv Booster $idx",
                    vaultType = "convex-crv-rewards",
                    stakedToken = stakedToken.toFungibleToken(),
                    rewardTokens = emptyList(),
                    farmType = ContractType.STAKING
                )
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CONVEX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}