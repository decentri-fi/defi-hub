package io.defitrack.protocol.aave.v2.farming

import arrow.core.nel
import arrow.core.nonEmptyListOf
import io.defitrack.claimable.AbstractClaimableMarketProvider
import io.defitrack.claimable.domain.ClaimableMarket
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v2.contract.IncentivesControllerContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AAVE)
class AaveV2IncentivesControllerMarketProvider : AbstractClaimableMarketProvider() {

    val stkAave = "0x4da27a545c0c5B758a6BA100e3a049001de870f5"
    val incentivesController = "0xd784927ff2f95ba542bfc824c8a8a98f3495f6b5"

    override suspend fun fetchClaimables(): List<ClaimableMarket> {
        val stakedAave = erC20Resource.getTokenInformation(Network.ETHEREUM, stkAave)

        val incentivesContract = IncentivesControllerContract(
            blockchainGatewayProvider.getGateway(Network.ETHEREUM),
            incentivesController
        )

        return ClaimableMarket(
            id = incentivesController,
            name = "Aave v2 incentives controller",
            network = Network.ETHEREUM,
            protocol = Protocol.AAVE_V2,
            claimableRewardFetchers = nonEmptyListOf(
                ClaimableRewardFetcher(
                    Reward(
                        stakedAave,
                        incentivesContract::getUserUnclaimedRewardsFn
                    ),
                    preparedTransaction = selfExecutingTransaction(incentivesContract::claimRewardsFn)
                )
            )
        ).nel()
    }
}