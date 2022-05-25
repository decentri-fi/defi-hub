package io.defitrack.protocol.maple.contract

import java.math.BigInteger

class ReserveToken(
    val name: String,
    val address: String,
    val decimals: Int,
    val liquidationBonus: BigInteger
)
