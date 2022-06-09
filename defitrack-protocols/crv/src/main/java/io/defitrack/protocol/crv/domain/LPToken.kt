package io.defitrack.protocol.crv.domain

import java.math.BigInteger

class LPToken(
    val id: String,
    val pool: Pool,
    val token: Token
) {

    class Pool(val coins: List<Coin>, val balances: List<BigInteger>)

    class Token(val id: String)

    class Coin(
        val id: String,
    )
}

