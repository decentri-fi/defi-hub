package io.defitrack.protocol.sushiswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiPolygonService
import io.defitrack.protocol.reward.MiniChefV2Contract
import io.defitrack.protocol.sushiswap.apr.MinichefStakingAprCalculator
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class SushiswapPolygonFarmingMinichefMarketProvider(
    private val abiResource: ABIResource,
    private val priceResource: PriceResource,
) : FarmingMarketProvider() {

    val minichefABI by lazy {
        abiResource.getABI("sushi/MiniChefV2.json")
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return SushiPolygonService.getMiniChefs().map {
            MiniChefV2Contract(
                getBlockchainGateway(),
                minichefABI,
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength()).map { poolId ->
                toStakingMarketElement(chef, poolId)
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    private suspend fun toStakingMarketElement(
        chef: MiniChefV2Contract,
        poolId: Int
    ): FarmingMarket {
        val stakedtoken = getToken(chef.getLpTokenForPoolId(poolId))
        val rewardToken = getToken(chef.rewardToken())
        return create(
            identifier = "${chef.address}-${poolId}",
            name = stakedtoken.name + " Farm",
            stakedToken = stakedtoken.toFungibleToken(),
            rewardTokens = listOf(
                rewardToken.toFungibleToken()
            ),
            vaultType = "sushi-minichefV2",
            marketSize = calculateMarketSize(chef, stakedtoken),
            apr = MinichefStakingAprCalculator(getERC20Resource(), priceResource, chef, poolId).calculateApr(),
            balanceFetcher = PositionFetcher(
                chef.address,
                { user -> chef.userInfoFunction(poolId, user) }
            ),
            farmType = FarmType.LIQUIDITY_MINING
        )
    }

    private suspend fun calculateMarketSize(
        chef: MiniChefV2Contract,
        stakedTokenInformation: TokenInformationVO
    ): BigDecimal {
        val balance = getERC20Resource().getBalance(getNetwork(), stakedTokenInformation.address, chef.address)
        return BigDecimal.valueOf(
            priceResource.calculatePrice(
                PriceRequest(
                    stakedTokenInformation.address,
                    getNetwork(),
                    balance.toBigDecimal()
                        .divide(BigDecimal.TEN.pow(stakedTokenInformation.decimals), 18, RoundingMode.HALF_UP),
                    TokenType.SUSHISWAP
                )
            )
        )
    }
}