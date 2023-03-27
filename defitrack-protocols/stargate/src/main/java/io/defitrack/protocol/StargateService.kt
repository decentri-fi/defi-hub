package io.defitrack.protocol

interface StargateService {
    fun getLpFarm(): String

    fun getPoolFactory(): String
}