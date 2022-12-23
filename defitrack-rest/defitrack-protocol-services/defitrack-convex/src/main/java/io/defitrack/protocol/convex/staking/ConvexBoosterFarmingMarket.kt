package io.defitrack.protocol.convex.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexService
import io.defitrack.protocol.convex.contract.ConvexBoosterContract
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class ConvexBoosterFarmingMarket(
    convexService: ConvexService,
    blockchainGatewayProvider: BlockchainGatewayProvider,
    abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
) : FarmingMarketProvider() {

    val booster = ConvexBoosterContract(
        blockchainGatewayProvider.getGateway(getNetwork()),
        abiResource.getABI("convex/Booster.json"),
        convexService.provideBooster()
    )

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val poolInfos = booster.poolInfos()

        List(poolInfos.size) { idx ->
            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), poolInfos[idx].lpToken)

            create(
                identifier = booster.address + "-" + idx,
                name = "Convex Crv Booster $idx",
                vaultType = "convex-crv-rewards",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = emptyList(),
                farmType = FarmType.STAKING
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