package io.defitrack.protocol

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.contract.HopStakingReward
import io.defitrack.protocol.staking.Token
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class HopPolygonStakingMarketService(
    private val hopService: HopService,
    private val erC20Resource: ERC20Resource,
    private val abiResource: ABIResource,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val priceResource: PriceResource
) : StakingMarketService() {
    override suspend fun fetchStakingMarkets(): List<StakingMarketElement> {
        return hopService.getStakingRewards(getNetwork()).map { stakingReward ->

            val pool = HopStakingReward(
                polygonContractAccessor,
                abiResource.getABI("quickswap/StakingRewards.json"),
                stakingReward
            )

            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), pool.stakingTokenAddress)
            val rewardToken = erC20Resource.getTokenInformation(getNetwork(), pool.rewardsTokenAddress)

            StakingMarketElement(
                id = "hop-polygon-${pool.address}",
                network = getNetwork(),
                protocol = getProtocol(),
                name = "${stakedToken.name} Staking Rewards",
                token = stakedToken.toStakedToken(),
                rewardToken = rewardToken.toRewardToken(),
                contractAddress = pool.address,
                vaultType = "hop-staking-rewards",
                marketSize = getMarketSize(stakedToken, pool)
            )
        }
    }

    private fun getMarketSize(
        stakedToken: Token,
        pool: HopStakingReward
    ) = BigDecimal.valueOf(
        priceResource.calculatePrice(
            PriceRequest(
                address = stakedToken.address,
                network = getNetwork(),
                amount = pool.totalSupply.toBigDecimal().divide(
                    BigDecimal.TEN.pow(stakedToken.decimals), RoundingMode.HALF_UP
                ),
                type = stakedToken.type
            )
        )
    )

    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}