package io.defitrack.protocol.seamless

import io.defitrack.protocol.aave.v3.AaveV3DataProvider
import org.springframework.stereotype.Component

@Component
class SeamlessAaveV3DataProvider : AaveV3DataProvider {
    override fun poolAddress(): String = "0x8F44Fd754285aa6A2b8B9B97739B79746e0475a7"
    override fun poolDataProvider(): String = "0x2A0979257105834789bC6b9E1B00446DFbA8dFBa"
}