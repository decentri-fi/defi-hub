package io.defitrack.protocol.maple.domain

import io.defitrack.protocol.maple.domain.MapleReserve
import java.math.BigInteger

class UserReserve(
    val reserve: MapleReserve,
    val currentATokenBalance: BigInteger,
    val currentVariableDebt: BigInteger,
    val currentStableDebt: BigInteger,
)
