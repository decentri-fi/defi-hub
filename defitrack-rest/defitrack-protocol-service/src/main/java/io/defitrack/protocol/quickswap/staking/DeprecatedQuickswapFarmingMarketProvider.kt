package io.defitrack.protocol.quickswap.staking

import arrow.core.Either
import arrow.core.Either.Companion.catch
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
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.QuickswapRewardPoolContract
import io.defitrack.protocol.quickswap.contract.RewardFactoryContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.coroutineScope
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

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = RewardFactoryContract(
            getBlockchainGateway(),
            quickswapService.getDeprecatedRewardFactory(),
        )

        return contract.getRewardPools().map {
            QuickswapRewardPoolContract(
                getBlockchainGateway(),
                it
            )
        }.parMapNotNull(EmptyCoroutineContext, 12) { rewardPool ->
            catch {
                createMarket(rewardPool)
            }.mapLeft {
                logger.error("Error while fetching quickswap market", it)
                null
            }.getOrNull()
        }
    }

    private suspend fun createMarket(rewardPool: QuickswapRewardPoolContract): FarmingMarket {
        val stakedToken = getToken(rewardPool.stakingTokenAddress.await())
        val rewardToken = getToken(rewardPool.rewardsTokenAddress.await())
        return create(
            identifier = rewardPool.address,
            name = "${stakedToken.name} Reward Pool (Deprecated)",
            stakedToken = stakedToken,
            rewardToken = rewardToken,
            marketSize = refreshable {
                getMarketSize(stakedToken, rewardPool)
            },
            claimableRewardFetcher = ClaimableRewardFetcher(
                Reward(
                    token = rewardToken,
                    contractAddress = rewardPool.address,
                    getRewardFunction = rewardPool::earned,
                ),
                preparedTransaction = selfExecutingTransaction(rewardPool::getRewardFunction)
            ),
            positionFetcher = defaultPositionFetcher(rewardPool.address),
            deprecated = true
        )
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

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}