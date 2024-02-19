package io.defitrack.config

import arrow.core.continuations.AtomicRef
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.web3j.protocol.Web3j
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

private const val default_latch_value = 10

@Component
class Web3JEndpoints(
    private val primaryWeb3j: Web3j,
    @Qualifier("fallbackWeb3js") private val fallbacks: List<Web3j>,
) {

    @Scheduled(fixedRate = 1000 * 60 * 60 * 4)
    fun resetLatch() {
        logger.info("resetting latch")
        countDownLatch.set(default_latch_value)
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    val primary: AtomicReference<Web3j> = AtomicRef(primaryWeb3j)
    val countDownLatch: AtomicInteger = AtomicInteger(default_latch_value)


    fun getPrimaryWeb3j(): Web3j {
        return primary.get()
    }

    fun getFallback(includesDefault: Boolean = false): Web3j? {
        val aFallBack = fallbacks.shuffled().firstOrNull()
        return if (aFallBack != null) {
            logger.info("using fallback: {}", aFallBack)
            aFallBack
        } else if (includesDefault) {
            logger.info("using default as fallback")
            getPrimaryWeb3j()
        } else {
            logger.info("no fallback found")
            null
        }
    }

    fun observePrimaryDown() {
        val latch = countDownLatch.getAndDecrement()
        logger.info("Primary was down, only $latch tries left.")
        if (latch < 1) {
            logger.debug("Primary was down, only $latch tries left, resetting latch and setting new endpoint")
            countDownLatch.set(default_latch_value)

            val newEndpoint = if (primary.get() == primaryWeb3j) getFallback(false) else primaryWeb3j
            logger.info("setting new endpoint: {}", newEndpoint)
            primary.set(newEndpoint)
        }
    }
}