package io.defitrack.token

import java.math.BigInteger

open class FungibleToken(
    val address: String,
    val name: String,
    val decimals: Int,
    val symbol: String,
    val logo: String?,
    val type: TokenType,
    val totalSupply: BigInteger
)