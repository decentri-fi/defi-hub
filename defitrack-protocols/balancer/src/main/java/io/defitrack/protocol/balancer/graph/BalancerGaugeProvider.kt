package io.defitrack.protocol.balancer.graph

import io.defitrack.protocol.balancer.domain.LiquidityGauge

interface BalancerGaugeProvider {

    suspend fun getGauges(): List<LiquidityGauge>


}