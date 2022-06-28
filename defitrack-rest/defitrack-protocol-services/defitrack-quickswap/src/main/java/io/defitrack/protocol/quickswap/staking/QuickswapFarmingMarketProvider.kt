package io.defitrack.protocol.quickswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapRewardPoolContract
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
class QuickswapFarmingMarketProvider(
    private val quickswapService: QuickswapService,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val abiService: ABIResource,
    private val priceResource: PriceResource,
    private val erC20Resource: ERC20Resource,
    private val quickswapAPRService: QuickswapAPRService,
) : FarmingMarketProvider() {

    val stakingRewardsABI by lazy {
        abiService.getABI("quickswap/StakingRewards.json")
    }

    override suspend fun fetchStakingMarkets(): List<FarmingMarket> {
        return quickswapService.getVaultAddresses().map {
            QuickswapRewardPoolContract(
                blockchainGatewayProvider.getGateway(getNetwork()),
                stakingRewardsABI,
                it
            )
        }.map { pool ->
            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), pool.stakingTokenAddress)
            val rewardToken = erC20Resource.getTokenInformation(getNetwork(), pool.rewardsTokenAddress)

            FarmingMarket(
                id = "quickswap-polygon-${pool.address}",
                network = getNetwork(),
                protocol = getProtocol(),
                name = "${stakedToken.name} Reward Pool",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(rewardToken.toFungibleToken()),
                contractAddress = pool.address,
                vaultType = "quickswap-reward-pool",
                marketSize = getMarketSize(stakedToken, pool),
                apr = (quickswapAPRService.getRewardPoolAPR(pool.address) + quickswapAPRService.getLPAPR(
                    stakedToken.address
                )),
                balanceFetcher = FarmingPositionFetcher(
                    pool.address,
                    { user -> pool.balanceOfMethod(user) }
                )
            )
        }
    }

    private suspend fun getMarketSize(
        stakedTokenInformation: TokenInformation,
        pool: QuickswapRewardPoolContract
    ) = BigDecimal.valueOf(
        priceResource.calculatePrice(
            PriceRequest(
                address = stakedTokenInformation.address,
                network = getNetwork(),
                amount = pool.totalSupply.toBigDecimal().divide(
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