package io.defitrack.invest

import java.math.BigInteger

class PrepareInvestmentCommand(
    val user: String,
    val amount: BigInteger?
)