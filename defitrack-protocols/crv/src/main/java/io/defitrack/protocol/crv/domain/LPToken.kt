package io.defitrack.protocol.crv.domain

import java.math.BigInteger

class LPToken(
    val id: String,
    val pool: Pool,
    val token: Token
) {

    class Pool(val coins: List<Coin>)

    class Token(
        val id: String,
        val name: String,
        val symbol: String,
        val decimals: Int
    )

    class Coin(
        val id: String,
    )
}

