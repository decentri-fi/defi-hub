package io.defitrack.protocol.maple.contract

import java.math.BigInteger

class UserAccountData(
    val totalCollateralETH: BigInteger,
    val totalDebtETH: BigInteger,
    val availableBorrowsETH: BigInteger,
    val currentLiquidationThreshold: BigInteger,
    val ltv: BigInteger,
    val healthFactor: BigInteger
)
