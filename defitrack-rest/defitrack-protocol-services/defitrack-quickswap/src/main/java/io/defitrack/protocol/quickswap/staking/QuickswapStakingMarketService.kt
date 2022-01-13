package io.defitrack.protocol.quickswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapRewardPoolContract
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.quickswap.QuickswapService
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class QuickswapStakingMarketService(
    private val quickswapService: QuickswapService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiService: ABIResource,
    private val priceResource: PriceResource,
    private val erC20Resource: ERC20Resource,
    private val quickswapAPRService: QuickswapAPRService,
) : StakingMarketService() {

    val stakingRewardsABI by lazy {
        abiService.getABI("quickswap/StakingRewards.json")
    }

    override fun fetchStakingMarkets(): List<StakingMarketElement> {
        return quickswapService.getVaultAddresses().map {
            QuickswapRewardPoolContract(
                polygonContractAccessor,
                stakingRewardsABI,
                it
            )
        }.map { pool ->
            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), pool.stakingTokenAddress)
            val rewardToken = erC20Resource.getTokenInformation(getNetwork(), pool.rewardsTokenAddress)

            StakingMarketElement(
                id = "quickswap-polygon-${pool.address}",
                network = getNetwork(),
                protocol = getProtocol(),
                name = "${stakedToken.name} Reward Pool",
                token = stakedToken.toStakedToken(),
                rewardToken = rewardToken.toRewardToken(),
                contractAddress = pool.address,
                vaultType = "quickswap-reward-pool",
                marketSize = priceResource.calculatePrice(
                    PriceRequest(
                        address = stakedToken.address,
                        network = getNetwork(),
                        amount = pool.totalSupply.toBigDecimal().divide(
                            BigDecimal.TEN.pow(stakedToken.decimals), RoundingMode.HALF_UP
                        ),
                        type = stakedToken.type
                    )
                ),
                rate = (quickswapAPRService.getRewardPoolAPR(pool.address) + quickswapAPRService.getLPAPR(
                    stakedToken.address
                )).toDouble()
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}