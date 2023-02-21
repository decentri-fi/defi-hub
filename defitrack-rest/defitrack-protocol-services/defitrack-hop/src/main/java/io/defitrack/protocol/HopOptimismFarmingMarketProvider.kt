package io.defitrack.protocol

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.contract.HopStakingReward
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class HopOptimismFarmingMarketProvider(
    private val hopService: HopService,
    private val abiResource: ABIResource,
    private val priceResource: PriceResource
) : FarmingMarketProvider() {
    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val semaphore = Semaphore(10)
        hopService.getStakingRewards(getNetwork()).map { stakingReward ->
            async {
                semaphore.withPermit {
                    toStakingMarket(stakingReward)
                }
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun toStakingMarket(stakingReward: String): FarmingMarket? {
        return try {
            val pool = HopStakingReward(
                getBlockchainGateway(),
                abiResource.getABI("quickswap/StakingRewards.json"),
                stakingReward
            )

            val stakedToken = getToken(pool.stakingTokenAddress())
            val rewardToken = getToken(pool.rewardsTokenAddress())

            return create(
                identifier = pool.address,
                name = "${stakedToken.name} Staking Rewards",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(rewardToken.toFungibleToken()),
                vaultType = "hop-staking-rewards",
                marketSize = getMarketSize(stakedToken, pool),
                balanceFetcher = PositionFetcher(
                    address = pool.address,
                    function = { user -> pool.balanceOfMethod(user) }
                ),
                farmType = ContractType.LIQUIDITY_MINING
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    private suspend fun getMarketSize(
        stakedTokenInformation: TokenInformationVO,
        pool: HopStakingReward
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
        return Protocol.HOP
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}