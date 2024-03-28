package io.defitrack.market.domain.position

import java.math.BigInteger

class ExitPositionCommand(
    val user: String,
    val amount: BigInteger
)