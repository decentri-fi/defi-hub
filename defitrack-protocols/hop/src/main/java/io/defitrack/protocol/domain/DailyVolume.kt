package io.defitrack.protocol.domain

import java.math.BigInteger

class DailyVolume(
    val id: String,
    val amount: BigInteger,
    val date: Int
)