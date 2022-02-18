package io.defitrack.token

class FungibleToken(
    val address: String,
    val name: String,
    val decimals: Int,
    val symbol: String,
    val type: TokenType
)