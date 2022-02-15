package io.defitrack.protocol.yearn

import java.math.BigInteger

class VaultDayData(
    val dayReturnsGeneratedUSDC: BigInteger,
    val dayReturnsGenerated: BigInteger,
    val tokenPriceUSDC: BigInteger
)