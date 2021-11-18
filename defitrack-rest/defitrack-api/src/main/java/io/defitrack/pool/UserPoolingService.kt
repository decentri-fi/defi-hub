package io.defitrack.pool

import io.defitrack.pool.domain.PoolingElement
import io.defitrack.protocol.ProtocolService

interface UserPoolingService : ProtocolService {
    fun userPoolings(address: String): List<PoolingElement>
}