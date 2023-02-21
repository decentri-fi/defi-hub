package io.defitrack.staking

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aelin.farming.StakingRewardsContract
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class AelinRewardMarketProvider(
    private val priceResource: PriceResource,
) : FarmingMarketProvider() {

    val aelinAddress = "0x61baadcf22d2565b0f471b291c475db5555e0b76"
    val rewardPool by lazy {
        StakingRewardsContract(getBlockchainGateway())
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val aelin = getToken(aelinAddress)

        return listOf(
            create(
                name = "aelin-staking",
                identifier = "aelin-staking",
                stakedToken = aelin.toFungibleToken(),
                rewardTokens = listOf(aelin.toFungibleToken()),
                vaultType = "staking-rewards",
                marketSize = marketSizeService.getMarketSize(
                    aelin.toFungibleToken(),
                    rewardPool.address,
                    getNetwork()
                ),
                apr = calculateSingleRewardPool(rewardPool.address),
                balanceFetcher = defaultPositionFetcher(
                    rewardPool.address
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    rewardPool.address,
                    { user ->
                        rewardPool.earned(user)
                    },
                    preparedTransaction = {
                        PreparedTransaction(
                            getNetwork().toVO(), rewardPool.getRewardFunction(), rewardPool.address
                        )
                    }
                ),
                farmType = FarmType.STAKING
            )
        )
    }

    private suspend fun calculateSingleRewardPool(address: String): BigDecimal {
        val quickRewardsPerYear =
            (rewardPool.rewardRate().times(BigInteger.valueOf(31536000))).toBigDecimal()
                .divide(BigDecimal.TEN.pow(18))
        val usdRewardsPerYear = priceResource.calculatePrice(
            PriceRequest(
                aelinAddress,
                getNetwork(),
                quickRewardsPerYear
            )
        ).toBigDecimal()

        val marketsize = marketSizeService.getMarketSize(
            getToken(aelinAddress).toFungibleToken(),
            address,
            getNetwork()
        )

        return if (usdRewardsPerYear == BigDecimal.ZERO || marketsize == BigDecimal.ZERO) {
            BigDecimal.ZERO
        } else {
            usdRewardsPerYear.divide(marketsize, 4, RoundingMode.HALF_UP)
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.AELIN
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}