package io.defitrack.aave

import java.math.BigInteger

class UserReserve(
    val reserve: AaveReserve,
    val currentATokenBalance: BigInteger,
    val currentVariableDebt: BigInteger,
    val currentStableDebt: BigInteger,
)