package io.codechef.defitrack.protocol.quickswap.staking

import io.codechef.common.network.Network
import io.codechef.defitrack.abi.ABIResource
import io.codechef.defitrack.price.PriceRequest
import io.codechef.defitrack.price.PriceService
import io.codechef.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.codechef.defitrack.staking.StakingMarketService
import io.codechef.defitrack.staking.domain.StakingMarketElement
import io.codechef.defitrack.token.TokenService
import io.codechef.matic.config.PolygonContractAccessor
import io.codechef.protocol.Protocol
import io.codechef.quickswap.QuickswapRewardPoolContract
import io.codechef.quickswap.QuickswapService
import okhttp3.internal.toImmutableList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Service
class QuickswapStakingMarketService(
    private val quickswapService: QuickswapService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiService: ABIResource,
    private val priceService: PriceService,
    private val tokenService: TokenService,
    private val quickswapAPRService: QuickswapAPRService,
) : StakingMarketService {

    val stakingRewardsABI by lazy {
        abiService.getABI("quickswap/StakingRewards.json")
    }

    val marketBuffer = mutableListOf<StakingMarketElement>()
    val executor = Executors.newCachedThreadPool()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @PostConstruct
    fun init() {
        quickswapService.getVaultAddresses().map {
            QuickswapRewardPoolContract(
                polygonContractAccessor,
                stakingRewardsABI,
                it
            )
        }.forEach { pool ->
            executor.submit {
                val stakedToken = tokenService.getTokenInformation(pool.stakingTokenAddress, getNetwork())
                val rewardToken = tokenService.getTokenInformation(pool.rewardsTokenAddress, getNetwork())

                val market = StakingMarketElement(
                    id = "quickswap-polygon-${pool.address}",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = "${stakedToken.name} Reward Pool",
                    token = stakedToken.toStakedToken(),
                    rewardToken = rewardToken.toRewardToken(),
                    contractAddress = pool.address,
                    vaultType = "quickswap-reward-pool",
                    marketSize = priceService.calculatePrice(
                        PriceRequest(
                            address = stakedToken.address,
                            network = getNetwork(),
                            amount = pool.totalSupply.toBigDecimal().divide(
                                BigDecimal.TEN.pow(stakedToken.decimals), RoundingMode.HALF_UP
                            ),
                            type = stakedToken.type
                        )
                    ),
                    rate = (quickswapAPRService.getRewardPoolAPR(pool.address) + quickswapAPRService.getLPAPR(
                        stakedToken.address
                    )).toDouble()
                )
                logger.info("imported ${market.id}")
                marketBuffer.add(market)
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