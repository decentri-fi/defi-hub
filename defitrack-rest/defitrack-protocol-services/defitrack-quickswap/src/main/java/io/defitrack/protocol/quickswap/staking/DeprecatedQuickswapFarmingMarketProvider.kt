package io.defitrack.protocol.quickswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.protocol.quickswap.contract.QuickswapRewardPoolContract
import io.defitrack.protocol.quickswap.contract.RewardFactoryContract
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class DeprecatedQuickswapFarmingMarketProvider(
    private val quickswapService: QuickswapService,
    private val abiService: ABIResource,
    private val priceResource: PriceResource,
    private val quickswapAPRService: QuickswapAPRService,
) : FarmingMarketProvider() {

    val rewardFactoryContract = lazyAsync {
        RewardFactoryContract(
            getBlockchainGateway(),
            quickswapService.getDeprecatedRewardFactory(),
        )
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val contract = rewardFactoryContract.await()
        val rewardPools = contract.getStakingTokens().map {
            contract.stakingRewardsInfoByStakingToken(it)
        }

        logger.info("importing ${rewardPools.size} reward pools")

        val semaphore = Semaphore(8)

        rewardPools.map {
            QuickswapRewardPoolContract(
                getBlockchainGateway(),
                it
            )
        }.map { rewardPool ->
            async {
                try {
                    semaphore.withPermit {
                        val stakedToken = getToken(rewardPool.stakingTokenAddress())
                        val rewardToken = getToken(rewardPool.rewardsTokenAddress())
                        create(
                            identifier = rewardPool.address,
                            name = "${stakedToken.name} Reward Pool (Deprecated)",
                            stakedToken = stakedToken.toFungibleToken(),
                            rewardTokens = listOf(rewardToken.toFungibleToken()),
                            vaultType = "quickswap-reward-rewardPool",
                            marketSize = Refreshable.refreshable {
                                getMarketSize(stakedToken, rewardPool)
                            },
                            apr = (quickswapAPRService.getRewardPoolAPR(rewardPool.address) + quickswapAPRService.getLPAPR(
                                stakedToken.address
                            )),
                            claimableRewardFetcher = ClaimableRewardFetcher(
                                rewardPool.address,
                                { user ->
                                    rewardPool.earned(user)
                                },
                                preparedTransaction = {
                                    PreparedTransaction(
                                        getNetwork().toVO(), rewardPool.getRewardFunction(), rewardPool.address
                                    )
                                }
                            ),
                            balanceFetcher = defaultPositionFetcher(
                                rewardPool.address
                            ),
                            farmType = ContractType.LIQUIDITY_MINING,
                            rewardsFinished = true
                        )
                    }
                } catch (ex: Exception) {
                    logger.error("Error while fetching reward pool: " + ex.message)
                    null
                }
            }
        }.awaitAll().filterNotNull()
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