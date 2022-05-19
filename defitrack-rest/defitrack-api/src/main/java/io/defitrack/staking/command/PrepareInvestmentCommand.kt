package io.defitrack.staking.command

import java.math.BigInteger

class PrepareInvestmentCommand(
    val user: String,
    val amount: BigInteger? = null
)