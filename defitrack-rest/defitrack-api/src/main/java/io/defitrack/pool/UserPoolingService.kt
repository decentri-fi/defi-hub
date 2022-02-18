package io.defitrack.pool

import io.defitrack.pool.domain.PoolingElement
import io.defitrack.protocol.ProtocolService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

abstract class UserPoolingService : ProtocolService {


    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(
        1.minutes
    ).build<String, List<PoolingElement>>()

    fun userPoolings(address: String): List<PoolingElement> {
        return runBlocking(Dispatchers.IO) {
            cache.get("${getProtocol().slug}-${getNetwork().slug}-$address") {
                fetchUserPoolings(address)
            }
        }
    }

    abstract suspend fun fetchUserPoolings(address: String): List<PoolingElement>
}