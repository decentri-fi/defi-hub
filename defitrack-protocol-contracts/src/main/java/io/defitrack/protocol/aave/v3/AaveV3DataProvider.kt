package io.defitrack.protocol.aave.v3

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class AaveV3DataProvider {

    val configs = mapOf(
        Network.ARBITRUM to AaveV3Config(
            poolAddress = "0x794a61358D6845594F94dc1DB02A252b5b4814aD",
            poolDataProvider = "0x69FA688f1Dc47d4B5d8029D5a35FB7a548310654"
        ),
        Network.ETHEREUM to AaveV3Config(
            "0x87870Bca3F3fD6335C3F4ce8392D69350B4fA4E2",
            "0x7B4EB56E7CD4b454BA8ff71E4518426369a138a3"
        ),
        Network.OPTIMISM to AaveV3Config(
            poolAddress = "0x794a61358D6845594F94dc1DB02A252b5b4814aD",
            poolDataProvider = "0x69FA688f1Dc47d4B5d8029D5a35FB7a548310654"
        ),
        Network.BASE to AaveV3Config(
            poolAddress = "0x43955b0899Ab7232E3a454cf84AedD22Ad46FD33",
            poolDataProvider = "0x2d8A3C5677189723C4cB8873CfC9C8976FDF38Ac"
        ),
        Network.POLYGON to AaveV3Config(
            poolAddress = "0x794a61358D6845594F94dc1DB02A252b5b4814aD",
            poolDataProvider = "0x69FA688f1Dc47d4B5d8029D5a35FB7a548310654"
        )
    )
}

data class AaveV3Config(
    val poolAddress: String,
    val poolDataProvider: String
)
