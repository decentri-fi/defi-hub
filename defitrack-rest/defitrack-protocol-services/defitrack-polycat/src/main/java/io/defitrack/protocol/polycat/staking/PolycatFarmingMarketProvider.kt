package io.defitrack.protocol.polycat.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.polycat.PolycatService
import io.defitrack.protocol.polycat.contract.PolycatMasterChefContract
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PolycatFarmingMarketProvider(
    private val polycatService: PolycatService,
) : FarmingMarketProvider() {

    val masterChefABI by lazy {
        runBlocking { getAbi("polycat/MasterChef.json") }
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return polycatService.getPolycatFarms().map {
            PolycatMasterChefContract(
                getBlockchainGateway(),
                masterChefABI,
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength()).map { poolId ->
                toStakingMarketElement(chef, poolId)
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.POLYCAT
    }

    private suspend fun toStakingMarketElement(
        chef: PolycatMasterChefContract,
        poolId: Int
    ): FarmingMarket {
        val stakedtoken = getToken(chef.poolInfo(poolId).lpToken)
        val rewardToken = getToken(chef.rewardToken())
        return create(
            identifier = "${chef.address}-${poolId}",
            name = stakedtoken.name + " Farm",
            stakedToken = stakedtoken.toFungibleToken(),
            rewardTokens = listOf(
                rewardToken.toFungibleToken()
            ),
            apr = PolygcatStakingAprCalculator(getERC20Resource(), getPriceResource(), chef, poolId).calculateApr(),
            marketSize = calculateMarketSize(stakedtoken, chef),
            balanceFetcher = PositionFetcher(
                address = chef.address,
                { user ->
                    chef.userInfoFunction(
                        user, poolId
                    )
                },
            ),
            vaultType = "polycat-masterchef",
            farmType = ContractType.LIQUIDITY_MINING
        )
    }

    private suspend fun calculateMarketSize(
        stakedtoken: TokenInformationVO,
        chef: PolycatMasterChefContract
    ): BigDecimal {

        val balance = getERC20Resource().getBalance(
            getNetwork(),
            stakedtoken.address,
            chef.address
        )

        return BigDecimal(
            getPriceResource().calculatePrice(
                PriceRequest(
                    stakedtoken.address,
                    getNetwork(),
                    balance.asEth(stakedtoken.decimals),
                    stakedtoken.type
                )
            )
        )
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}