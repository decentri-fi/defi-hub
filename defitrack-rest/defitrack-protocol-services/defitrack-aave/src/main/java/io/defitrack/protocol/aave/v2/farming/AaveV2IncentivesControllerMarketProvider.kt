package io.defitrack.protocol.aave.v2.farming

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v2.contract.IncentivesControllerContract
import org.springframework.stereotype.Service

@Service
class AaveV2IncentivesControllerMarketProvider : FarmingMarketProvider() {

    val stkAave = "0x4da27a545c0c5B758a6BA100e3a049001de870f5"
    val incentivesController = "0xd784927ff2f95ba542bfc824c8a8a98f3495f6b5"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val stkAaveToken = getToken(stkAave)
        return listOf(
            create(
                name = "Aave v2 incentives controller",
                identifier = "aave-v2-incentives-controller",
                stakedToken = stkAaveToken.toFungibleToken(),
                rewardTokens = listOf(stkAaveToken.toFungibleToken()),
                vaultType = "aave-v2-incentives",
                marketSize = getMarketSize(stkAaveToken.toFungibleToken(), incentivesController),
                balanceFetcher = PositionFetcher(
                    incentivesController, { user ->
                        IncentivesControllerContract(
                            getBlockchainGateway(),
                            incentivesController
                        ).getUserUnclaimedRewardsFn(user)
                    }
                ),
                farmType = ContractType.STAKING
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.AAVE_V2
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}