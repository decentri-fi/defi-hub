package io.codechef.defitrack.pool

import io.codechef.defitrack.pool.domain.PoolingElement
import io.codechef.defitrack.protocol.ProtocolService

interface UserPoolingService : ProtocolService {
    fun userPoolings(address: String): List<PoolingElement>
}