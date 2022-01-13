package io.defitrack.staking

import io.defitrack.protocol.ProtocolService
import io.defitrack.protocol.staking.Token
import io.defitrack.staking.domain.RewardToken
import io.defitrack.staking.domain.StakedToken
import io.defitrack.staking.domain.StakingMarketElement
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

abstract class StakingMarketService : ProtocolService {

    val logger = LoggerFactory.getLogger(this.javaClass)

    @OptIn(ExperimentalTime::class)
    val cache =
        Cache.Builder().expireAfterWrite(Duration.Companion.hours(4)).build<String, List<StakingMarketElement>>()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun init() {
        cache.invalidateAll()
        Executors.newSingleThreadExecutor().submit {
            getStakingMarkets()
        }
    }

    fun getStakingMarkets(): List<StakingMarketElement> = runBlocking(Dispatchers.IO) {
        cache.get("all") {
            logger.info("Cache empty or expired, fetching fresh elements")
            val elements = fetchStakingMarkets()
            logger.info("Cache successfuly filled with ${elements.size} elements")
            elements
        }
    }

    abstract fun fetchStakingMarkets(): List<StakingMarketElement>


    fun Token.toStakedToken(): StakedToken {
        return StakedToken(
            name = this.name,
            symbol = this.symbol,
            address = this.address,
            network = getNetwork(),
            decimals = this.decimals,
            type = this.type
        )
    }


    fun Token.toRewardToken(): RewardToken {
        return RewardToken(
            name = this.name,
            symbol = this.symbol,
            decimals = this.decimals,
        )
    }
}