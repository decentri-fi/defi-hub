package io.defitrack.protocol.sushiswap.pooling

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiPolygonService
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.reward.MiniChefV2Contract
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.RewardToken
import io.defitrack.staking.domain.StakedToken
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.TokenService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class SushiswapPolygonStakingMinichefMarketService(
    private val abiResource: ABIResource,
    private val tokenService: TokenService,
    private val polygonContractAccessor: PolygonContractAccessor,
): StakingMarketService {


    @OptIn(ExperimentalTime::class)
    val cache =
        Cache.Builder().expireAfterWrite(Duration.Companion.hours(1)).build<String, List<StakingMarketElement>>()

    val minichefABI by lazy {
        abiResource.getABI("sushi/MiniChefV2.json")
    }

    @PostConstruct
    fun init() {
        Executors.newSingleThreadExecutor().submit {
            getStakingMarkets()
        }
    }

    override fun getStakingMarkets(): List<StakingMarketElement> {
        return runBlocking(Dispatchers.IO) {
            cache.get("all") {
                SushiPolygonService.getMiniChefs().map {
                    MiniChefV2Contract(
                        polygonContractAccessor,
                        minichefABI,
                        it
                    )
                }.flatMap { chef ->
                    (0 until chef.poolLength).map { poolId ->
                        toStakingMarketElement(chef, poolId)
                    }
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
       return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    private fun toStakingMarketElement(
        chef: MiniChefV2Contract,
        poolId: Int
    ): StakingMarketElement {
        val rewardPerBlock = chef.rewardPerBlock.toBigDecimal().times(BigDecimal(43200)).times(BigDecimal(365))
        val stakedtoken =
            tokenService.getTokenInformation(chef.getLpTokenForPoolId(poolId), getNetwork())
        val rewardToken = tokenService.getTokenInformation(chef.rewardToken, getNetwork())
        return StakingMarketElement(
            id = "sushi-${chef.address}-${poolId}",
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
            vaultType = "sushi-minichefV2",
            marketSize = 0.0,
            rate = 0.0
        )
    }
}