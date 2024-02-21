package io.defitrack.protocol.pendle

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component


@Component
class PendleAddressBook {

    data class PendleAddresses(
        val ptOracleContract: String,
        val marketFactoryV3: String
    )


    val addresses: Map<Network, PendleAddresses>
        get() {
            return mapOf(
                Network.ARBITRUM to PendleAddresses(
                    ptOracleContract = "0x7e16e4253CE4a1C96422a9567B23b4b5Ebc207F1",
                    marketFactoryV3 = "0x2FCb47B58350cD377f94d3821e7373Df60bD9Ced"
                ),
                Network.ETHEREUM to PendleAddresses(
                    ptOracleContract = "0xbbd487268A295531d299c125F3e5f749884A3e30",
                    marketFactoryV3 = "0x1A6fCc85557BC4fB7B534ed835a03EF056552D52"
                )
            )
        }
}