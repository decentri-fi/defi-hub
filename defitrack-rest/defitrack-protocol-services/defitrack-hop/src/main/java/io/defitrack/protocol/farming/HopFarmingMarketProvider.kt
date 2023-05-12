package io.defitrack.protocol.farming

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.HopService
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.HopStakingReward
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

abstract class HopFarmingMarketProvider(
    private val hopService: HopService,
) : FarmingMarketProvider() {

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        hopService.getStakingRewards(getNetwork()).forEach { stakingReward ->
            launch {
                throttled {
                    toStakingMarket(stakingReward)?.let { send(it) }
                }
            }
        }
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
                    function = { user -> balanceOfFunction(user) }
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
        getPriceResource().calculatePrice(
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
}