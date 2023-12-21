package io.defitrack.protocol.aave.staking

import arrow.core.nel
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Type
import java.math.BigDecimal
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.AAVE)
class StABPTStakingMarketProvider : FarmingMarketProvider() {

    private val stABPT = "0xa1116930326d21fb917d5a27f1e9943a9595fb47"
    private val aave = "0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9"
    private val abpt = "0x41a08648c3766f9f9d85598ff102a08f4ef84f84"
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val aaveToken = getToken(aave)
        val abptToken = getToken(abpt)

        val stakingContract = StakedAaveContract(
            getBlockchainGateway(),
            stABPT
        )

        return create(
            name = "stABPT",
            identifier = "stABPT",
            stakedToken = abptToken,
            rewardToken = aaveToken,
            marketSize = refreshable {
                getMarketSize(abptToken, stABPT)
            },
            positionFetcher = PositionFetcher(
                stakingContract::balanceOfFunction,
                stakingContract.extractBalanceFunction {
                    getRatio(stakingContract)
                },
            ),
            claimableRewardFetcher = ClaimableRewardFetcher(
                Reward(
                    token = aaveToken,
                    getRewardFunction = stakingContract::getTotalRewardFunction
                ),
                preparedTransaction = selfExecutingTransaction(stakingContract::getClaimRewardsFunction)
            )
        ).nel()
    }

    private suspend fun StABPTStakingMarketProvider.getRatio(stakingContract: StakedAaveContract): BigDecimal {
        val totalStakedAbpt = getBalance(abpt, stABPT)
        return totalStakedAbpt.toBigDecimal().dividePrecisely(stakingContract.totalSupply().get().toBigDecimal())
    }

    override fun getProtocol(): Protocol {
        return Protocol.AAVE_V3
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}