package io.defitrack.protocol.kyberswap.domain

import io.defitrack.protocol.kyberswap.domain.LiquidityPosition

class User(
    val id: String,
    val liquidityPositions: List<LiquidityPosition>
)