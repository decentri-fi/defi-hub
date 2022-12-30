package io.defitrack.protocol.quickswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.protocol.quickswap.contract.QuickswapDualRewardPoolContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class QuickswapDualFarmingMarketProvider(
    private val quickswapService: QuickswapService,
    private val abiService: ABIResource,
    private val priceResource: PriceResource,
    private val quickswapAPRService: QuickswapAPRService,
) : FarmingMarketProvider() {

    val stakingRewardsABI by lazy {
        abiService.getABI("quickswap/DualStakingRewards.json")
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        quickswapService.getDualPools().map {
            QuickswapDualRewardPoolContract(
                getBlockchainGateway(),
                stakingRewardsABI,
                it
            )
        }.map { pool ->
            async {
                try {
                    val stakedToken = erC20Resource.getTokenInformation(getNetwork(), pool.stakingTokenAddress())
                    val rewardTokenA = erC20Resource.getTokenInformation(getNetwork(), pool.rewardsTokenAddressA())
                    val rewardTokenB = erC20Resource.getTokenInformation(getNetwork(), pool.rewardsTokenAddressB())

                    create(
                        identifier = pool.address,
                        name = "${stakedToken.name} Dual Reward Pool",
                        stakedToken = stakedToken.toFungibleToken(),
                        rewardTokens = listOf(
                            rewardTokenA.toFungibleToken(),
                            rewardTokenB.toFungibleToken()
                        ),
                        vaultType = "quickswap-dual-reward-pool",
                        marketSize = getMarketSize(stakedToken, pool),
                        apr = getApr(pool, stakedToken),
                        balanceFetcher = PositionFetcher(
                            pool.address,
                            { user -> pool.balanceOfMethod(user) }
                        ),
                        farmType = FarmType.DUAL_REWARD_MINING
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun getApr(
        pool: QuickswapDualRewardPoolContract,
        stakedTokenInformation: TokenInformationVO
    ): BigDecimal {
        return (quickswapAPRService.getDualPoolAPR(pool.address) + quickswapAPRService.getLPAPR(
            stakedTokenInformation.address
        ))
    }

    private suspend fun getMarketSize(
        stakedTokenInformation: TokenInformationVO,
        pool: QuickswapDualRewardPoolContract
    ) = BigDecimal.valueOf(
        priceResource.calculatePrice(
            PriceRequest(
                address = stakedTokenInformation.address,
                network = getNetwork(),
                amount = pool.totalSupply().toBigDecimal().divide(
                    BigDecimal.TEN.pow(stakedTokenInformation.decimals), RoundingMode.HALF_UP
                ),
                type = stakedTokenInformation.type
            )
        )
    )

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}