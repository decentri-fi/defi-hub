package io.defitrack.protocol.plutusdao.staking

import arrow.core.nel
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.plutusdao.PlutusRouterContract
import io.defitrack.protocol.plutusdao.StakedPLSContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.PLUTUSDAO)
class StakedPlsFarmingMarketProvider : FarmingMarketProvider() {

    val routerAddress = "0xca24cf44c863f7709b7ea0c08ff88b994063684b"
    val stakedPls = "0xe9645988a5e6d5efcc939bed1f3040dba94c6cbb"
    val plsAddress = "0x51318b7d00db7acc4026c88c3952b66278b6a67f"
    val esPlsAddress = "0xc636c1f678df0a834ad103196338cb7dd1d194ff"

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val pls = getToken(plsAddress)
        val esPls = getToken(esPlsAddress)

        val contract = StakedPLSContract(getBlockchainGateway(), stakedPls)
        val router = PlutusRouterContract(getBlockchainGateway(), routerAddress)

        return create(
            name = "Staked PLS",
            identifier = stakedPls,
            stakedToken = pls,
            rewardToken = esPls,
            positionFetcher = PositionFetcher(
                contract::stakedAmounts
            ),
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