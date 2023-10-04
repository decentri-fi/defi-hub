package io.defitrack.protocol.hop.domain

data class HopLpToken(
    val lpToken: String,
    val hToken: String,
    val swapAddress: String,
    val canonicalToken: String
)