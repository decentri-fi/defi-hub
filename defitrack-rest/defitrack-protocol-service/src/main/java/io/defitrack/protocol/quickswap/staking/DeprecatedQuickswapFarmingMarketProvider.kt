package io.defitrack.protocol.quickswap.staking

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.price.port.`in`.PricePort
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.QuickswapRewardPoolContract
import io.defitrack.protocol.quickswap.contract.RewardFactoryContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.QUICKSWAP)
class DeprecatedQuickswapFarmingMarketProvider(
    private val quickswapService: QuickswapService,
    private val priceResource: PricePort,
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
                    getRewardFunction = rewardPool::earned,
                ),
                preparedTransaction = selfExecutingTransaction(rewardPool::getRewardFunction)
            ),
            positionFetcher = defaultPositionFetcher(rewardPool.address),
            deprecated = true
        )
    }

    private suspend fun getMarketSize(
        stakedTokenInformation: FungibleTokenInformation,
        pool: QuickswapRewardPoolContract
    ) = BigDecimal.valueOf(
        priceResource.calculatePrice(
            GetPriceCommand(
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