package io.defitrack.protocol.dfyn.domain

import io.defitrack.protocol.dfyn.domain.LiquidityPosition

class User(
    val id: String,
    val liquidityPositions: List<LiquidityPosition>
)