package io.defitrack.protocol

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.contract.HopStakingReward
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarketBalanceFetcher
import io.defitrack.staking.domain.StakingMarket
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class HopPolygonStakingMarketService(
    private val hopService: HopService,
    private val erC20Resource: ERC20Resource,
    private val abiResource: ABIResource,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val priceResource: PriceResource
) : StakingMarketService() {
    override suspend fun fetchStakingMarkets(): List<StakingMarket> = coroutineScope {
        hopService.getStakingRewards(getNetwork()).map { stakingReward ->
            async(Dispatchers.IO.limitedParallelism(10)) {
                toStakingMarket(stakingReward)

            }
        }.awaitAll().filterNotNull()
    }

    private fun toStakingMarket(stakingReward: String): StakingMarket? {
        return try {
            val pool = HopStakingReward(
                contractAccessorGateway.getGateway(getNetwork()),
                abiResource.getABI("quickswap/StakingRewards.json"),
                stakingReward
            )

            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), pool.stakingTokenAddress)
            val rewardToken = erC20Resource.getTokenInformation(getNetwork(), pool.rewardsTokenAddress)

            return StakingMarket(
                id = "hop-polygon-${pool.address}",
                network = getNetwork(),
                protocol = getProtocol(),
                name = "${stakedToken.name} Staking Rewards",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(rewardToken.toFungibleToken()),
                contractAddress = pool.address,
                vaultType = "hop-staking-rewards",
                marketSize = getMarketSize(stakedToken, pool),
                balanceFetcher = StakingMarketBalanceFetcher(
                    address = pool.address,
                    function = { user -> pool.balanceOfMethod(user) }
                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    private fun getMarketSize(
        stakedTokenInformation: TokenInformation,
        pool: HopStakingReward
    ) = BigDecimal.valueOf(
        priceResource.calculatePrice(
            PriceRequest(
                address = stakedTokenInformation.address,
                network = getNetwork(),
                amount = pool.totalSupply.toBigDecimal().divide(
                    BigDecimal.TEN.pow(stakedTokenInformation.decimals), RoundingMode.HALF_UP
                ),
                type = stakedTokenInformation.type
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