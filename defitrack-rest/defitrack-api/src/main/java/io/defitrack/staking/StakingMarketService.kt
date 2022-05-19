package io.defitrack.staking

import io.defitrack.protocol.ProtocolService
import io.defitrack.staking.domain.StakingMarket
import io.defitrack.token.FungibleToken
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.math.BigDecimal
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.hours

abstract class StakingMarketService : ProtocolService {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    val cache =
        Cache.Builder().expireAfterWrite(4.hours).build<String, List<StakingMarket>>()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun init() {
        try {
            cache.invalidateAll()
            Executors.newSingleThreadExecutor().submit {
                getStakingMarkets()
            }
        } catch (ex: Exception) {
            logger.error("something went wrong trying to populate the cache", ex)
        }
    }

    fun getStakingMarkets(): List<StakingMarket> = runBlocking(Dispatchers.IO) {
        cache.get("all") {
            try {
                logger.info("Cache empty or expired, fetching fresh elements")
                val elements = fetchStakingMarkets()
                logger.info("Cache successfuly filled with ${elements.size} elements")
                elements
            } catch (ex: Exception) {
                logger.error("Unable to fetch staking markets: {}", ex.message)
                emptyList()
            }
        }
    }

    protected abstract suspend fun fetchStakingMarkets(): List<StakingMarket>

    fun stakingMarket(
        id: String,
        name: String,
        stakedToken: FungibleToken,
        rewardTokens: List<FungibleToken>,
        contractAddress: String,
        vaultType: String,
        marketSize: BigDecimal = BigDecimal.ZERO,
        rate: BigDecimal = BigDecimal.ZERO
    ): StakingMarket {
        return StakingMarket(
            id = id,
            network = getNetwork(),
            protocol = getProtocol(),
            name = name,
            stakedToken = stakedToken,
            rewardTokens = rewardTokens,
            contractAddress = contractAddress,
            vaultType = vaultType,
            marketSize = marketSize,
            apr = rate
        )
    }
}