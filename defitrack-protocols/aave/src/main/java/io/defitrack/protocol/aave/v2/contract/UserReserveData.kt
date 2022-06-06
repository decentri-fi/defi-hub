package io.defitrack.protocol.aave.v2.contract

import java.math.BigInteger

class UserReserveData(
    val currentATokenBalance: BigInteger,
    val currentStableDebt: BigInteger,
    val currentVariableDebt: BigInteger,
    val principalStableDebt: BigInteger,
    val scaledVariableDebt: BigInteger,
    val stableBorrowRate: BigInteger,
    val liquidityRate: BigInteger,
    val stableRateLastUpdated: BigInteger,
    val usageAsCollateralEnabled: Boolean,
    val asset: ReserveToken
)