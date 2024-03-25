package io.defitrack.protocol.seamless

import io.defitrack.common.network.Network
import io.defitrack.protocol.aave.v3.AaveV3Config
import org.springframework.stereotype.Component

@Component
class SeamlessAaveV3DataProvider {
    val configs = mapOf(
        Network.BASE to AaveV3Config(
            poolAddress = "0x8F44Fd754285aa6A2b8B9B97739B79746e0475a7",
            poolDataProvider = "0x2A0979257105834789bC6b9E1B00446DFbA8dFBa"
        )
    )
}