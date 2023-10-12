package io.defitrack.vo

import java.math.BigInteger

class TransactionVO(
    val hash: String,
    val blockNumber: BigInteger,
    val from: String,
    val to: String?,
    val time: Long,
    val value: BigInteger,
    val possibleSpam: Boolean,
)