package io.defitrack.protocol.kyberswap.graph.domain

class User(
    val id: String,
    val liquidityPositions: List<LiquidityPosition>
)