package io.defitrack.protocol.quickswap.apr

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceResource
import io.defitrack.protocol.quickswap.QuickswapRewardPoolContract
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.QuickswapDualRewardPoolContract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.time.Duration.Companion.hours

@Component
class QuickswapAPRService(
    private val quickswapService: QuickswapService,
    private val abiResource: ABIResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val priceResource: PriceResource,
) {

    val stakingRewardsABI by lazy {
       runBlocking {
           abiResource.getABI("quickswap/StakingRewards.json")
       }
    }

    val stakingDualRewards by lazy {
       runBlocking {
           abiResource.getABI("quickswap/DualStakingRewards.json")
       }
    }

    val cache = Cache.Builder().expireAfterWrite(
        1.hours
    ).build<String, BigDecimal>()

    suspend fun getDualPoolAPR(address: String): BigDecimal {
        return cache.get("dual-rewardpool-$address") {
            calculateDualRewardPool(address)
        }
    }

    suspend fun getRewardPoolAPR(address: String): BigDecimal {
        return cache.get("rewardpool-$address") {
            calculateSingleRewardPool(address)
        }
    }

    private suspend fun calculateDualRewardPool(address: String): BigDecimal {
        val contract = QuickswapDualRewardPoolContract(
            blockchainGatewayProvider.getGateway(Network.POLYGON),
            stakingDualRewards,
            address
        )
        val quickRewardsPerYear =
            (contract.rewardRateA().times(BigInteger.valueOf(31536000))).toBigDecimal()
                .divide(BigDecimal.TEN.pow(18))
        val usdQuickRewardsPerYear = priceResource.getPrice("QUICK").times(
            quickRewardsPerYear
        )

        val maticRewardsPerYear =
            (contract.rewardRateB().times(BigInteger.valueOf(31536000))).toBigDecimal()
                .divide(BigDecimal.TEN.pow(18))
        val usdMaticRewardsPerYear = priceResource.getPrice("MATIC").times(
            maticRewardsPerYear
        )

        val stakingTokenAddress = contract.stakingTokenAddress()
        val reserveUsd = quickswapService.getPairs().find {
            it.id.lowercase() == stakingTokenAddress
        }?.reserveUSD ?: BigDecimal.ZERO

        return if ((usdQuickRewardsPerYear == BigDecimal.ZERO && usdMaticRewardsPerYear == BigDecimal.ZERO) || reserveUsd == BigDecimal.ZERO) {
            BigDecimal.ZERO
        } else {
            (usdQuickRewardsPerYear.plus(usdMaticRewardsPerYear)).divide(reserveUsd, 6, RoundingMode.HALF_UP)
        }
    }

    private suspend fun calculateSingleRewardPool(address: String): BigDecimal {
        val contract = QuickswapRewardPoolContract(
            blockchainGatewayProvider.getGateway(Network.POLYGON),
            stakingRewardsABI,
            address
        )

        val quickRewardsPerYear =
            (contract.rewardRate().times(BigInteger.valueOf(31536000))).toBigDecimal()
                .divide(BigDecimal.TEN.pow(18))
        val usdRewardsPerYear = priceResource.getPrice("DQUICK").times(
            quickRewardsPerYear
        )

        val stakingTokenAddress = contract.stakingTokenAddress()
        val reserveUsd = quickswapService.getPairs().find {
            it.id.lowercase() == stakingTokenAddress
        }?.reserveUSD ?: BigDecimal.ZERO

        return if (usdRewardsPerYear == BigDecimal.ZERO || reserveUsd == BigDecimal.ZERO) {
            BigDecimal.ZERO
        } else {
            usdRewardsPerYear.divide(reserveUsd, 4, RoundingMode.HALF_UP)
        }
    }

    suspend fun getLPAPR(address: String): BigDecimal {
        return cache.get("lp-$address") {
            try {
                val pairData = quickswapService.getPairDayData(address)
                if (pairData.size <= 1) {
                    BigDecimal.ZERO
                } else {
                    pairData.drop(1).map {
                        it.dailyVolumeUSD
                    }.reduce { a, b -> a.plus(b) }
                        .times(BigDecimal.valueOf(0.003)).times(BigDecimal.valueOf(52))
                        .divide(
                            quickswapService.getPairs().find {
                                it.id == address
                            }!!.reserveUSD,
                            18,
                            RoundingMode.HALF_UP
                        )
                }
            } catch (ex: Exception) {
                BigDecimal.ZERO
            }
        }
    }
}