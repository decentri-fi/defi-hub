package io.defitrack.protocol.sushi.domain

class SushiUser(
    val id: String,
    val liquidityPositions: List<LiquidityPosition>
)