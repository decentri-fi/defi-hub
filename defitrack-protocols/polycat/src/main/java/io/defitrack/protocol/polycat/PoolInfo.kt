package io.defitrack.protocol.polycat

import java.math.BigInteger

class PoolInfo(
    val lpToken: String,
    val allocPoint: BigInteger,
    val lastRewardBlock: BigInteger,
    val accFishPerShare: BigInteger,
    val depositFeeBP: BigInteger
)