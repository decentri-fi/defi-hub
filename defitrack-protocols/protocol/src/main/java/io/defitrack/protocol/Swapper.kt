package io.defitrack.protocol

import java.math.BigInteger

interface Swapper {

    fun getExpectedTokens(
        from: String,
        to: String,
        amount: BigInteger
    ): BigInteger
}