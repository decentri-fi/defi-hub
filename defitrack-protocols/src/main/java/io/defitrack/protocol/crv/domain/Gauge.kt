package io.defitrack.protocol.crv.domain

import java.math.BigDecimal

class Gauge(
    val address: String,
    val pool: Pool?
) {
    class Pool(
        val id: String,
        val name: String,
        val swapAddress: String,
        val virtualPrice: String,
        val lpToken: LpToken,
        val coins: List<Coin>
    )

    class LpToken(
        val id: String,
        val address: String,
        val decimals: Int,
        val name: String,
        val symbol: String
    )

    class Coin(
        val id: String,
        val underlying: UnderlyingCoin
    )

    class UnderlyingCoin(
        val id: String,
        val index: Int,
        val balance: BigDecimal,
        val token: CrvToken
    )

    class CrvToken(
        val address: String,
        val decimals: Int,
        val name: String? = "?",
        val symbol: String? = "?",
    )
}