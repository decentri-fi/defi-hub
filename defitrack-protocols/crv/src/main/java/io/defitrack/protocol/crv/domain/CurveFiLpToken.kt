package io.defitrack.protocol.crv.domain

class CurveFiLpToken(
    val pool: Pool,
    val id: String,
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int
) {
    class Pool(val coins: List<Coin>)

    class Coin(
        val id: String,
        val token: Token
    )

    class Token(
        val id: String
    )
}