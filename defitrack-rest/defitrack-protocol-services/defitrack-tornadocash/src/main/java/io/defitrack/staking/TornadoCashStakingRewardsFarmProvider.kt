package io.defitrack.staking

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.tornadocash.farming.StakingRewardsContract
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Component
class TornadoCashStakingRewardsFarmProvider : FarmingMarketProvider() {

    val stakingRewards = "0x720ffb58b4965d2c0bd2b827fa8316c2002a98aa"

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val stakingRewardsContract = StakingRewardsContract(getBlockchainGateway(), stakingRewards)


        return listOf(
            create(
                name = "staking-rewards-${stakingRewards}",
                identifier = stakingRewards,
                farmType = ContractType.STAKING,
                rewardTokens = listOf(
                    getToken(stakingRewardsContract.rewardsToken()).toFungibleToken()
                ),
                stakedToken = getToken(stakingRewardsContract.stakingToken()).toFungibleToken(),
                vaultType = "staking-rewards",
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
            PriceRequest(
                stakingRewardsContract.rewardsToken(),
                getNetwork(),
                quickRewardsPerYear
            )
        ).toBigDecimal()

        val marketsize = marketSizeService.getMarketSize(
            getToken(stakingRewardsContract.stakingToken()).toFungibleToken(),
            stakingRewardsContract.address,
            getNetwork()
        )

        return if (usdRewardsPerYear == BigDecimal.ZERO || marketsize == BigDecimal.ZERO) {
            BigDecimal.ZERO
        } else {
            usdRewardsPerYear.divide(marketsize, 4, RoundingMode.HALF_UP)
        }
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}