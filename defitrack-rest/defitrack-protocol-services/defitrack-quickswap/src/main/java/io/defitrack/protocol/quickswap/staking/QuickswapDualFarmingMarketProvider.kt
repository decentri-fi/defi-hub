package io.defitrack.protocol.quickswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.contract.QuickswapDualRewardPoolContract
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingPositionFetcher
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class QuickswapDualFarmingMarketProvider(
    private val quickswapService: QuickswapService,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val abiService: ABIResource,
    private val priceResource: PriceResource,
    private val quickswapAPRService: QuickswapAPRService,
    private val erC20Resource: ERC20Resource,
) : FarmingMarketProvider() {

    val stakingRewardsABI by lazy {
        abiService.getABI("quickswap/DualStakingRewards.json")
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return quickswapService.getDualPools().map {
            QuickswapDualRewardPoolContract(
                blockchainGatewayProvider.getGateway(getNetwork()),
                stakingRewardsABI,
                it
            )
        }.mapNotNull { pool ->
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
                    balanceFetcher = FarmingPositionFetcher(
                        pool.address,
                        { user -> pool.balanceOfMethod(user) }
                    )
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }
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