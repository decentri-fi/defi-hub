package io.defitrack.protocol.quickswap.staking

import io.codechef.defitrack.price.PriceRequest
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.codechef.defitrack.staking.StakingMarketService
import io.codechef.defitrack.staking.domain.StakingMarketElement
import io.codechef.defitrack.token.TokenService
import io.defitrack.abi.ABIResource
import io.defitrack.abi.PriceResource
import io.defitrack.common.network.Network
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapDualRewardPoolContract
import io.defitrack.quickswap.QuickswapService
import okhttp3.internal.toImmutableList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Service
class QuickswapDualStakingMarketService(
    private val quickswapService: QuickswapService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiService: ABIResource,
    private val priceResource: PriceResource,
    private val tokenService: TokenService,
    private val quickswapAPRService: QuickswapAPRService,
) : StakingMarketService {

    val stakingRewardsABI by lazy {
        abiService.getABI("quickswap/DualStakingRewards.json")
    }

    val marketBuffer = mutableListOf<StakingMarketElement>()
    val executor = Executors.newCachedThreadPool()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @PostConstruct
    fun init() {
        quickswapService.getDualPools().map {
            QuickswapDualRewardPoolContract(
                polygonContractAccessor,
                stakingRewardsABI,
                it
            )
        }.forEach { pool ->
            executor.submit {
                try {
                    val stakedToken = tokenService.getTokenInformation(pool.stakingTokenAddress, getNetwork())
                    val rewardTokenA = tokenService.getTokenInformation(pool.rewardsTokenAddressA, getNetwork())
                    val rewardTokenB = tokenService.getTokenInformation(pool.rewardsTokenAddressB, getNetwork())

                    val market = StakingMarketElement(
                        id = "quickswap-polygon-dual-${pool.address}",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        name = "${stakedToken.name} Dual Reward Pool",
                        token = stakedToken.toStakedToken(),
                        rewardToken = rewardTokenA.toRewardToken(),
                        contractAddress = pool.address,
                        vaultType = "quickswap-dual-reward-pool",
                        marketSize = priceResource.calculatePrice(
                            PriceRequest(
                                address = stakedToken.address,
                                network = getNetwork(),
                                amount = pool.totalSupply.toBigDecimal().divide(
                                    BigDecimal.TEN.pow(stakedToken.decimals), RoundingMode.HALF_UP
                                ),
                                type = stakedToken.type
                            )
                        ),
                        rate = (quickswapAPRService.getDualPoolAPR(pool.address) + quickswapAPRService.getLPAPR(
                            stakedToken.address
                        )).toDouble()
                    )
                    logger.info("imported ${market.id}")
                    marketBuffer.add(market)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun getStakingMarkets(): List<StakingMarketElement> {
        return marketBuffer.toImmutableList()
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}