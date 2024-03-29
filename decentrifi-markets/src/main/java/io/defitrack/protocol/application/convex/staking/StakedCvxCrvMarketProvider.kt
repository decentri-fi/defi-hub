package io.defitrack.protocol.application.convex.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.contract.CvxCrvStakingWrapperContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.CONVEX)
class StakedCvxCrvMarketProvider : FarmingMarketProvider() {

    val stakingWrapperAddress = "0xaa0c3f5f7dfd688c6e646f66cd2a6b66acdbe434"


    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = getCvxCrvStakingWrapperContract()

        val cvxCrv = getToken(contract.cvxCrv.await())
        send(
            create(
                name = "Staked CvxCrv",
                identifier = stakingWrapperAddress,
                stakedToken = cvxCrv,
                rewardTokens = emptyList(),
                type = "convex.staked-cvxcrv",
                positionFetcher = defaultPositionFetcher(stakingWrapperAddress),
            )
        )
    }

    private fun getCvxCrvStakingWrapperContract() = with(getBlockchainGateway()) {
        CvxCrvStakingWrapperContract(
            stakingWrapperAddress
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.CONVEX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}