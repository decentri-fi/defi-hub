package io.defitrack.protocol.quickswap.staking

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.port.output.PriceClient
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

@Component
@ConditionalOnCompany(Company.QUICKSWAP)
@ConditionalOnNetwork(Network.POLYGON)
class OldQuickswapFarmingMarketProvider(
    private val quickswapService: QuickswapService,
    private val priceResource: PriceClient,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val contract = RewardFactoryContract(
            getBlockchainGateway(),
            quickswapService.getOldRewardFactory(),
        )

        contract.getRewardPools().map {
            quickswapRewardPoolContract(it)
        }.map { rewardPool ->
            val stakedToken = getToken(rewardPool.stakingTokenAddress.await())
            val rewardToken = getToken(rewardPool.rewardsTokenAddress.await())
            create(
                identifier = rewardPool.address,
                name = "${stakedToken.name} Reward Pool (Old)",
                stakedToken = stakedToken,
                rewardToken = rewardToken,
                marketSize = refreshable {
                    getMarketSize(stakedToken, rewardPool)
                },
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = rewardToken,
                        rewardPool::earned,
                    ),
                    preparedTransaction = selfExecutingTransaction(rewardPool::getRewardFunction)
                ),
                positionFetcher = defaultPositionFetcher(
                    rewardPool.address
                ),
                deprecated = true,
                type = "quickswap.farming",
                exitPositionPreparer = prepareExit {
                    rewardPool.exitFunction(it.amount)
                }
            )
        }
    }

    private fun quickswapRewardPoolContract(address: String) = with(getBlockchainGateway()) {
        QuickswapRewardPoolContract(
            address
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
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

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}