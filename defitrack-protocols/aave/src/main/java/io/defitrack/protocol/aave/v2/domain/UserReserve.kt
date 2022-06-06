package io.defitrack.protocol.aave.v2.domain

import io.defitrack.protocol.aave.v2.domain.AaveReserve
import java.math.BigInteger

class UserReserve(
    val reserve: AaveReserve,
    val currentATokenBalance: BigInteger,
    val currentVariableDebt: BigInteger,
    val currentStableDebt: BigInteger,
)