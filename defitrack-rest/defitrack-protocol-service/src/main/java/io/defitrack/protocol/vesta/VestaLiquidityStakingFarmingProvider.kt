package io.defitrack.protocol.vesta

import arrow.core.nel
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@Component
@ConditionalOnCompany(Company.VESTA)
class VestaLiquidityStakingFarmingProvider : FarmingMarketProvider() {

    val liquidityStakingAddress = "0x65207da01293c692a37f59d1d9b1624f0f21177c"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = VestaLiquidityStakingContract(
            getBlockchainGateway(),
            liquidityStakingAddress
        )

        val stakedToken = getToken(contract.stakingToken.await())
        val rewardToken = getToken(contract.vsta.await())

        return create(
            name = "Vesta Liquidity Staking",
            identifier = liquidityStakingAddress,
            stakedToken = stakedToken,
            rewardToken = rewardToken,
            deprecated = LocalDateTime.from(
                Date(contract.lastTimeRewardApplicable.await().toLong() * 1000).toInstant().atZone(ZoneId.systemDefault())
            ) < LocalDateTime.now(),
            positionFetcher = PositionFetcher(contract::balances),
            claimableRewardFetcher = ClaimableRewardFetcher(
                reward = Reward(
                    rewardToken,
                    contract::earned
                ),
                preparedTransaction = selfExecutingTransaction(contract::getReward)
            )
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.VESTA
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}