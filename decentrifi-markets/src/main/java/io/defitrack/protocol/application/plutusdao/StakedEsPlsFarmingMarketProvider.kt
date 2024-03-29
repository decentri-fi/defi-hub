package io.defitrack.protocol.application.plutusdao

import arrow.core.nel
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.plutusdao.PlutusRouterContract
import io.defitrack.protocol.plutusdao.StakedPLSContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.PLUTUSDAO)
class StakedEsPlsFarmingMarketProvider : FarmingMarketProvider() {

    val routerAddress = "0xca24cf44c863f7709b7ea0c08ff88b994063684b"
    val stakedPls = "0x4808f4828edbf64d867d3d6c161962290da1ff9c"
    val esPlsAddress = "0xc636c1f678df0a834ad103196338cb7dd1d194ff"
    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val esPls = getToken(esPlsAddress)

        val contract = StakedPLSContract(getBlockchainGateway(), stakedPls)
        val router = PlutusRouterContract(getBlockchainGateway(), routerAddress)

        return create(
            name = "Staked esPLS",
            identifier = stakedPls,
            stakedToken = esPls,
            rewardToken = esPls,
            positionFetcher = PositionFetcher(
                contract::stakedAmounts
            ),
            type = "plutusdao.staked-esPLS",
            claimableRewardFetcher = ClaimableRewardFetcher(
                reward = Reward(
                    esPls,
                    contract::claimable
                ),
                preparedTransaction = selfExecutingTransaction(router::claimEsPls)
            )
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.PLUTUSDAO
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}