package io.defitrack.protocol.stargate

interface StargateService {
    fun getLpFarm(): String

    fun getLpStakingTimeFarm(): String = ""

    fun getPoolFactory(): String
}