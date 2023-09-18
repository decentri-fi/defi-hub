package io.defitrack.protocol.aave.v3

interface AaveV3DataProvider {

    fun poolAddress(): String
    fun poolDataProvider(): String
}