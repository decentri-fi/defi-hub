package io.defitrack.protocol.farming

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.claimable.Reward
import io.defitrack.common.utils.Refreshable
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.HopService
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.HopStakingRewardContract
import io.defitrack.transaction.PreparedTransaction
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
            val contract = HopStakingRewardContract(
                getBlockchainGateway(),
                stakingReward
            )

            val stakedToken = getToken(contract.stakingTokenAddress())
            val rewardToken = getToken(contract.rewardsTokenAddress())

            return create(
                identifier = contract.address,
                name = "${stakedToken.name} Staking Rewards",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(rewardToken.toFungibleToken()),
                vaultType = "hop-staking-rewards",
                marketSize = Refreshable.refreshable {
                    getMarketSize(stakedToken, contract)
                },
                balanceFetcher = PositionFetcher(
                    address = contract.address,
                    function = { user -> balanceOfFunction(user) }
                ),
                farmType = ContractType.LIQUIDITY_MINING,
                metadata = mapOf(
                    "contract" to contract
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = rewardToken.toFungibleToken(),
                        contractAddress = contract.address,
                        getRewardFunction = { user -> contract.earnedFn(user) },
                    ),
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            getNetwork().toVO(),
                            contract.getRewardFn(),
                            contract.address,
                            user
                        )
                    }
                ),
            )
        } catch (ex: Exception) {
            logger.info("Error while fetching staking market $stakingReward", ex)
            null
        }
    }

    private suspend fun getMarketSize(
        stakedTokenInformation: TokenInformationVO,
        pool: HopStakingRewardContract
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