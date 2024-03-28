package io.defitrack.market.port.`in`

import io.defitrack.market.domain.pooling.PoolingPosition

interface PoolingPositions {
    suspend fun getUserPoolings(protocol: String, address: String): List<PoolingPosition>
}