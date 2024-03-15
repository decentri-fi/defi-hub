package io.defitrack.protocol.application.tornadocash

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.tornadocash.StakingRewardsContract
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Component
@ConditionalOnCompany(Company.TORNADO_CASH)
class TornadoCashStakingRewardsFarmProvider : FarmingMarketProvider() {

    val stakingRewards = "0x720ffb58b4965d2c0bd2b827fa8316c2002a98aa"

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val stakingRewardsContract = StakingRewardsContract(getBlockchainGateway(), stakingRewards)


        val rewardToken = getToken(stakingRewardsContract.rewardsToken())
        val stakingToken = getToken(stakingRewardsContract.stakingToken())

        return listOf(
            create(
                name = "${stakingToken.symbol} Staking Rewards",
                identifier = stakingRewards,
                rewardToken = rewardToken,
                stakedToken = stakingToken,
                type = "tornadocash.staking-rewards",
                apr = calculateSingleRewardPool(stakingRewardsContract),
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.TORNADO_CASH
    }

    private suspend fun calculateSingleRewardPool(stakingRewardsContract: StakingRewardsContract): BigDecimal {
        val blocksPerYear = BigInteger.valueOf(31536000).divide(12L.toBigInteger())
        val quickRewardsPerYear =
            (stakingRewardsContract.rewardRate().times(blocksPerYear)).toBigDecimal()
                .divide(BigDecimal.TEN.pow(18))
        val usdRewardsPerYear = getPriceResource().calculatePrice(
            GetPriceCommand(
                stakingRewardsContract.rewardsToken(),
                getNetwork(),
                quickRewardsPerYear
            )
        ).toBigDecimal()

        val marketsize = marketSizeService.getMarketSize(
            getToken(stakingRewardsContract.stakingToken()),
            stakingRewardsContract.address,
            getNetwork()
        )

        return if (usdRewardsPerYear > BigDecimal.ZERO && marketsize.usdAmount > BigDecimal.ZERO) {
            usdRewardsPerYear.divide(marketsize.usdAmount, 4, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}