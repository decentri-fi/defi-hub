package io.defitrack.protocol.balancer

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol

interface BalancerPoolGraphProvider {
    suspend fun getPool(poolAddress: String): Pool?

    suspend fun getPools(): List<Pool>

    fun getNetwork(): Network
    fun getProtocol(): Protocol = Protocol.BALANCER
}