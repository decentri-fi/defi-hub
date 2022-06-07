package io.defitrack.protocol.ribbon.domain

import java.math.BigInteger

class RibbonVault(
    val id: String,
    val name: String,
    val symbol: String,
    val underlyingAsset: String,
    val totalBalance: BigInteger
)