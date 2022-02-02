package io.defitrack.protocol.polycat.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.polycat.PolycatMasterChefContract
import io.defitrack.polycat.PolycatService
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.RewardToken
import io.defitrack.staking.domain.StakedToken
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PolycatStakingMarketService(
    private val polycatService: PolycatService,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    private val polygonContractAccessor: PolygonContractAccessor,
) : StakingMarketService() {

    val masterChefABI by lazy {
        abiResource.getABI("polycat/MasterChef.json")
    }

    override suspend fun fetchStakingMarkets(): List<StakingMarketElement> {
        return polycatService.getPolycatFarms().map {
            PolycatMasterChefContract(
                polygonContractAccessor,
                masterChefABI,
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength).map { poolId ->
                toStakingMarketElement(chef, poolId)
            }
        }
    }

    private fun toStakingMarketElement(
        chef: PolycatMasterChefContract,
        poolId: Int
    ): StakingMarketElement {
        val rewardPerBlock = chef.rewardPerBlock.toBigDecimal().times(BigDecimal(43200)).times(BigDecimal(365))
        val stakedtoken =
            erC20Resource.getTokenInformation(getNetwork(), chef.getLpTokenForPoolId(poolId))
        val rewardToken = erC20Resource.getTokenInformation(getNetwork(), chef.rewardToken)
        return StakingMarketElement(
            id = "polycat-${chef.address}-${poolId}",
            network = getNetwork(),
            name = stakedtoken.name + " Farm",
            protocol = getProtocol(),
            token = StakedToken(
                name = stakedtoken.name,
                symbol = stakedtoken.symbol,
                address = stakedtoken.address,
                network = getNetwork(),
                decimals = stakedtoken.decimals,
                type = stakedtoken.type
            ),
            rewardToken = RewardToken(
                name = rewardToken.name,
                symbol = rewardToken.symbol,
                decimals = rewardToken.decimals
            ),
            contractAddress = chef.address,
            vaultType = "polycat-masterchef",
            marketSize = 0.0,
            rate = 0.0
        )
    }


    override fun getProtocol(): Protocol {
        return Protocol.POLYCAT
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}