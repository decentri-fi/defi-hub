package io.defitrack.protocol.aave.staking

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.Position
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.AAVE)
class StABPTStakingMarketProvider(
) : FarmingMarketProvider() {

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

        val totalStakedAbpt = getERC20Resource().getBalance(getNetwork(), abpt, stABPT)
        val ratio = totalStakedAbpt.toBigDecimal().dividePrecisely(stakingContract.totalSupply().toBigDecimal())

        return listOf(
            create(
                name = "stABPT",
                identifier = "stABPT",
                stakedToken = abptToken.toFungibleToken(),
                rewardTokens = listOf(
                    aaveToken.toFungibleToken()
                ),
                marketSize = refreshable {
                    getMarketSize(
                        abptToken.toFungibleToken(), stABPT,
                    )
                },
                apr = null,
                balanceFetcher = PositionFetcher(
                    stABPT,
                    { user ->
                        balanceOfFunction(user)
                    },
                    { retVal ->
                        val userStAave = (retVal[0].value as BigInteger)
                        Position(
                            userStAave.toBigDecimal().times(ratio).toBigInteger(),
                            userStAave
                        )
                    }
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = aaveToken.toFungibleToken(),
                        contractAddress = stABPT,
                        getRewardFunction = { user ->
                            stakingContract.getTotalRewardFunction(user)
                        }
                    ),
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            getNetwork().toVO(),
                            stakingContract.getClaimRewardsFunction(user),
                            stABPT
                        )
                    }
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.AAVE_V3
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}