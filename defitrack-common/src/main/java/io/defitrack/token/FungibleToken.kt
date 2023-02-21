package io.defitrack.token

open class FungibleToken(
    val address: String,
    val name: String,
    val decimals: Int,
    val symbol: String,
    val logo: String?,
    val type: TokenType
)