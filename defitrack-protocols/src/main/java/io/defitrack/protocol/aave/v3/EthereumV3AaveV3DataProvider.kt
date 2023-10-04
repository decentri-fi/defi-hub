package io.defitrack.protocol.aave.v3

import org.springframework.stereotype.Component

@Component
class EthereumV3AaveV3DataProvider : AaveV3DataProvider {
    override fun poolAddress(): String = "0x87870Bca3F3fD6335C3F4ce8392D69350B4fA4E2"
    override fun poolDataProvider(): String = "0x7B4EB56E7CD4b454BA8ff71E4518426369a138a3"
}