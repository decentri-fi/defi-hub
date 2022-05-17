package io.defitrack.protocol.sushiswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiPolygonService
import io.defitrack.protocol.reward.MiniChefV2Contract
import io.defitrack.protocol.sushiswap.apr.MinichefStakingAprCalculator
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarketBalanceFetcher
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class SushiswapPolygonStakingMinichefMarketService(
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
    private val contractAccessorGateway: ContractAccessorGateway
) : StakingMarketService() {

    val minichefABI by lazy {
        abiResource.getABI("sushi/MiniChefV2.json")
    }

    override suspend fun fetchStakingMarkets(): List<StakingMarketElement> {
        return SushiPolygonService.getMiniChefs().map {
            MiniChefV2Contract(
                contractAccessorGateway.getGateway(getNetwork()),
                minichefABI,
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength).map { poolId ->
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
    ): StakingMarketElement {
        val stakedtoken =
            erC20Resource.getTokenInformation(getNetwork(), chef.getLpTokenForPoolId(poolId))
        val rewardToken = erC20Resource.getTokenInformation(getNetwork(), chef.rewardToken)
        return StakingMarketElement(
            id = "sushi-${chef.address}-${poolId}",
            network = getNetwork(),
            name = stakedtoken.name + " Farm",
            protocol = getProtocol(),
            stakedToken = stakedtoken.toFungibleToken(),
            rewardTokens = listOf(
                rewardToken.toFungibleToken()
            ),
            contractAddress = chef.address,
            vaultType = "sushi-minichefV2",
            marketSize = calculateMarketSize(chef, stakedtoken),
            apr = MinichefStakingAprCalculator(erC20Resource, priceResource, chef, poolId).calculateApr(),
            balanceFetcher = StakingMarketBalanceFetcher(
                chef.address,
                { user -> chef.userInfoFunction(poolId, user) }
            )
        )
    }

    private fun calculateMarketSize(chef: MiniChefV2Contract, stakedTokenInformation: TokenInformation): BigDecimal {
        val balance = erC20Resource.getBalance(getNetwork(), stakedTokenInformation.address, chef.address)
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