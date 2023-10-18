package io.defitrack.protocol.quickswap.staking

import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.protocol.quickswap.contract.QuickswapRewardPoolContract
import io.defitrack.protocol.quickswap.contract.RewardFactoryContract
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.QUICKSWAP)
class DeprecatedQuickswapFarmingMarketProvider(
    private val quickswapService: QuickswapService,
    private val priceResource: PriceResource,
) : FarmingMarketProvider() {

    val rewardFactoryContract = lazyAsync {
        RewardFactoryContract(
            getBlockchainGateway(),
            quickswapService.getDeprecatedRewardFactory(),
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
        }.parMapNotNull(EmptyCoroutineContext, 12) { rewardPool ->
            try {
                val stakedToken = getToken(rewardPool.stakingTokenAddress())
                val rewardToken = getToken(rewardPool.rewardsTokenAddress())
                create(
                    identifier = rewardPool.address,
                    name = "${stakedToken.name} Reward Pool (Deprecated)",
                    stakedToken = stakedToken.toFungibleToken(),
                    rewardTokens = listOf(rewardToken.toFungibleToken()),
                    marketSize = refreshable {
                        getMarketSize(stakedToken, rewardPool)
                    },
                    claimableRewardFetcher = ClaimableRewardFetcher(
                        Reward(
                            token = rewardToken.toFungibleToken(),
                            contractAddress = rewardPool.address,
                            getRewardFunction = { user ->
                                rewardPool.earned(user)
                            },
                        ),
                        preparedTransaction = selfExecutingTransaction(rewardPool::getRewardFunction)
                    ),
                    balanceFetcher = defaultPositionFetcher(
                        rewardPool.address
                    ),
                    rewardsFinished = true
                )
            } catch (ex: Exception) {
                logger.error("Error while fetching reward pool: " + ex.message, ex)
                null
            }
        }
    }

    private suspend fun getMarketSize(
        stakedTokenInformation: TokenInformationVO,
        pool: QuickswapRewardPoolContract
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