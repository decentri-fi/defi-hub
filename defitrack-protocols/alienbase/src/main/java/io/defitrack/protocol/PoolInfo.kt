package io.defitrack.protocol

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