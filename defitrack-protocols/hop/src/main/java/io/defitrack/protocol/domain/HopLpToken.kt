package io.defitrack.protocol.domain

data class HopLpToken(
    val lpToken: String,
    val hToken: String,
    val swapAddress: String,
    val canonicalToken: String
)