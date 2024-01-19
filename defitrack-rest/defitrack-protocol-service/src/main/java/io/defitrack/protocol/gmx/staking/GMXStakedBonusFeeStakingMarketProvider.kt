package io.defitrack.protocol.gmx.staking

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.gmx.StakedGMXContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnCompany(Company.GMX)
class GMXStakedBonusFeeStakingMarketProvider : FarmingMarketProvider() {

    private val stakedBonusandFeeGMX = "0xd2d1162512f927a7e282ef43a362659e4f2a728f"
    private val bonusGMX = "0x35247165119b69a40edd5304969560d0ef486921"
    private val stakedAndBonusGMX = "0x4d268a7d4c16ceb5a606c173bd974984343fea13"
    private val stakedGMX = "0x908c4d94d34924765f1edc22a1dd098397c59dd4"

    private val gmx = "0xfc5a1a6eb076a2c7ad06ed22c90d7e710e35ad0a"

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = StakedGMXContract(getBlockchainGateway(), stakedBonusandFeeGMX)
        val rewardToken = getToken(contract.rewardToken.await())

        /*
        val sbfGMX = getToken(stakedBonusandFeeGMX)
        val sbGMX = getToken(stakedAndBonusGMX)
        val sGMX = getToken(stakedGMX)
        val bGMX = getToken(bonusGMX)

        val sbAmount = getBalance(sbGMX.address, sbfGMX.address)

        val sAmount = getBalance(sGMX.address, sbGMX.address)
        val bAmount = getBalance(bGMX.address, sbGMX.address)
        val feeGMXAmount = getBalance(bGMX.address, sbfGMX.address)

        val sbRatio = sbAmount.dividePrecisely(sbGMX.totalSupply.toBigDecimal())

        val ratioForStaked = sAmount.dividePrecisely(sGMX.totalSupply.toBigDecimal())
        val ratioForBonus = bAmount.toBigDecimal().dividePrecisely(bGMX.totalSupply.toBigDecimal())


        val totalAmountStaked = getBalance(gmx, sGMX.address).toBigDecimal().times(ratioForStaked).times(sbRatio)
        val totalBonusAmount = bAmount.toBigDecimal().times(ratioForBonus)
        val totalFeeAmount = feeGMXAmount

        logger.info(
            """
            
            sbAmount: $sbAmount
            sAmount: $sAmount
            bAmount: $bAmount
            feeGMXAmount: $feeGMXAmount
            
            total amount staked: $totalAmountStaked
            total bonus amount: $totalBonusAmount
            total fee amount: $totalFeeAmount
        """.trimIndent()
        )

         */
        send(
            create(
                name = "Staked GMX",
                identifier = "staked-gmx-$stakedBonusandFeeGMX",
                stakedToken = getToken(gmx),
                rewardToken = rewardToken,
                marketSize = refreshable(BigDecimal.ZERO),
                positionFetcher = PositionFetcher(contract::balanceOfFunction),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = rewardToken,
                        getRewardFunction = contract::claimableFn,
                    ),
                    preparedTransaction = selfExecutingTransaction(contract::claimFn)
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.GMX
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}