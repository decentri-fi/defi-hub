package io.defitrack.protocol.aave.domain

import io.defitrack.protocol.aave.domain.AaveReserve
import java.math.BigInteger

class UserReserve(
    val reserve: AaveReserve,
    val currentATokenBalance: BigInteger,
    val currentVariableDebt: BigInteger,
    val currentStableDebt: BigInteger,
)