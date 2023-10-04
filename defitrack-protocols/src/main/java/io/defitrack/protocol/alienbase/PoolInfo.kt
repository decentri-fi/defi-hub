package io.defitrack.protocol.alienbase

import java.math.BigInteger

class PoolInfo(
    val lpToken: String,
    val allocPoint: BigInteger,
    val lastRewardBlock: BigInteger,
    val accAlbPerShare: BigInteger,
    val depositFeeBP: BigInteger,
    val harvestInterval: BigInteger,
    val totalLp: BigInteger
)