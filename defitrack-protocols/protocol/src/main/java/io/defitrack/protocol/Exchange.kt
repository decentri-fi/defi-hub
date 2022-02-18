package io.defitrack.protocol

import java.math.BigInteger


class Exchange(
    private val swapper: Swapper,
    val chain: Chain,
    val name: String,
    val type: Type,
    val index: Long
) {

    fun getExpectedTokens(
        from: String,
        to: String,
        amount: BigInteger
    ): BigInteger = try {
        swapper.getExpectedTokens(from, to, amount)
    } catch (exc: Exception) {
        BigInteger.ZERO
    }
}

enum class Chain {
    ETHEREUM_MAINNET, MATIC, BSC
}

enum class Type {
    AMM, STABLE
}