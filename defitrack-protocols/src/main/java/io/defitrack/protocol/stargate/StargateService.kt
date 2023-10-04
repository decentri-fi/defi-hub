package io.defitrack.protocol.stargate

interface StargateService {
    fun getLpFarm(): String

    fun getPoolFactory(): String
}