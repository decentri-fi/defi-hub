package io.defitrack.protocol.quickswap.staking

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.QuickswapRewardPoolContract
import io.defitrack.protocol.quickswap.contract.RewardFactoryContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
@ConditionalOnCompany(Company.QUICKSWAP)
class OldQuickswapFarmingMarketProvider(
    private val quickswapService: QuickswapService,
    private val priceResource: PriceResource,
) : FarmingMarketProvider() {

    val rewardFactoryContract = lazyAsync {
        RewardFactoryContract(
            getBlockchainGateway(),
            quickswapService.getOldRewardFactory(),
        )
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val contract = rewardFactoryContract.await()
        val rewardPools = contract.readMultiCall(
            contract.getStakingTokens().map {
                contract.stakingRewardsInfoByStakingToken(it)
            }
        ).filter { it.success }
            .map { it.data[0].value as String }

        rewardPools.map {
            QuickswapRewardPoolContract(
                getBlockchainGateway(),
                it
            )
        }.map { rewardPool ->
            async {
                try {
                    val stakedToken = getToken(rewardPool.stakingTokenAddress())
                    val rewardToken = getToken(rewardPool.rewardsTokenAddress())
                    create(
                        identifier = rewardPool.address,
                        name = "${stakedToken.name} Reward Pool (Old)",
                        stakedToken = stakedToken.toFungibleToken(),
                        rewardTokens = listOf(rewardToken.toFungibleToken()),
                        marketSize = refreshable {
                            getMarketSize(stakedToken, rewardPool)
                        },
                        claimableRewardFetcher = ClaimableRewardFetcher(
                            Reward(
                                token = rewardToken.toFungibleToken(),
                                rewardPool.address,
                                { user ->
                                    rewardPool.earned(user)
                                },
                            ),
                            preparedTransaction = selfExecutingTransaction(rewardPool::getRewardFunction)
                        ),
                        positionFetcher = defaultPositionFetcher(
                            rewardPool.address
                        ),
                        rewardsFinished = true,
                        exitPositionPreparer = prepareExit {
                            PreparedExit(
                                rewardPool.exitFunction(it.amount),
                                rewardPool.address,
                            )
                        }
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    private suspend fun getMarketSize(
        stakedTokenInformation: TokenInformationVO,
        pool: QuickswapRewardPoolContract
    ) = BigDecimal.valueOf(
        priceResource.calculatePrice(
            PriceRequest(
                address = stakedTokenInformation.address,
                network = getNetwork(),
                amount = pool.totalSupply().get().toBigDecimal().divide(
                    BigDecimal.TEN.pow(stakedTokenInformation.decimals), RoundingMode.HALF_UP
                ),
                type = stakedTokenInformation.type
            )
        )
    )

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}