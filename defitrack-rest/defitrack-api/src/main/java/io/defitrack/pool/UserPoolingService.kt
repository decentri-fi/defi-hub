package io.defitrack.pool

import io.defitrack.pool.domain.PoolingElement
import io.defitrack.protocol.ProtocolService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

abstract class UserPoolingService : ProtocolService {


    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.minutes(1)
    ).build<String, List<PoolingElement>>()

    fun userPoolings(address: String): List<PoolingElement> {
        return runBlocking {
            cache.get("${getProtocol().slug}-${getNetwork().slug}-$address") {
                fetchUserPoolings(address)
            }
        }
    }

    abstract fun fetchUserPoolings(address: String): List<PoolingElement>
}