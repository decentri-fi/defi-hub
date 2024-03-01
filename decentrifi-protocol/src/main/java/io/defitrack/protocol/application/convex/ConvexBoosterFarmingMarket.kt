package io.defitrack.protocol.application.convex

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexEthereumService
import io.defitrack.protocol.convex.contract.ConvexBoosterContract
import kotlinx.coroutines.coroutineScope
import org.springframework.cglib.core.Block
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.CONVEX)
class ConvexBoosterFarmingMarket(
    private val convexService: ConvexEthereumService,
) : FarmingMarketProvider() {


    context(BlockchainGateway)
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = ConvexBoosterContract(
            convexService.provideBooster()
        )
        return coroutineScope {
            val poolInfos = contract.poolInfos()

            List(poolInfos.size) { idx ->
                val stakedToken = getToken(poolInfos[idx].lpToken)
                create(
                    identifier = contract.address + "-" + idx,
                    name = "Convex ${stakedToken.name} Booster",
                    stakedToken = stakedToken,
                    type = "convex.booster",
                    rewardTokens = emptyList(),
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