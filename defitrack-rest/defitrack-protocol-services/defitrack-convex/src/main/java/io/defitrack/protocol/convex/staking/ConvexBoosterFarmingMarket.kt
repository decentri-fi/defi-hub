package io.defitrack.protocol.convex.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexEthereumService
import io.defitrack.protocol.convex.contract.ConvexBoosterContract
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class ConvexBoosterFarmingMarket(
    convexService: ConvexEthereumService,
    abiResource: ABIResource,
) : FarmingMarketProvider() {

    val booster by lazy {
        runBlocking {
            ConvexBoosterContract(
                getBlockchainGateway(),
                abiResource.getABI("convex/Booster.json"),
                convexService.provideBooster()
            )
        }
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val poolInfos = booster.poolInfos()

        List(poolInfos.size) { idx ->
            val stakedToken = getToken(poolInfos[idx].lpToken)

            create(
                identifier = booster.address + "-" + idx,
                name = "Convex Crv Booster $idx",
                vaultType = "convex-crv-rewards",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = emptyList(),
                farmType = ContractType.STAKING
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CONVEX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}