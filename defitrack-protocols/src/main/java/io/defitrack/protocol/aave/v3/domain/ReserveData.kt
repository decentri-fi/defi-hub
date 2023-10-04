package io.defitrack.protocol.aave.v3.domain

import java.math.BigInteger

class ReserveData(
    val unbacked: BigInteger,
    val accruedToTreasuryScaled: BigInteger,
    val totalAToken: BigInteger,
    val totalStableDebt: BigInteger,
    val totalVariableDebt: BigInteger,
    val liquidityRate: BigInteger,
    val variableBorrowRate: BigInteger,
    val stableBorrowRate: BigInteger,
    val averageStableBorrowRate: BigInteger,
    val liquidityIndex: BigInteger,
    val variableBorrowIndex: BigInteger,
    val lastUpdateTimestamp: BigInteger
)