package io.defitrack.protocol.balancer.domain

import java.math.BigDecimal

class LiquidityMiningReward(
    val user: String,
    val token: String,
    val amount: BigDecimal,
    val week: Int
)