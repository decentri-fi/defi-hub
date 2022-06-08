package io.defitrack.protocol.aave.v3

import org.springframework.stereotype.Component

@Component
class AaveV3OptimismDataProvider {

    fun getPoolAddress(): String {
        return "0x794a61358D6845594F94dc1DB02A252b5b4814aD"
    }

    fun getPoolDataProvider(): String {
        return "0x69FA688f1Dc47d4B5d8029D5a35FB7a548310654"
    }
}