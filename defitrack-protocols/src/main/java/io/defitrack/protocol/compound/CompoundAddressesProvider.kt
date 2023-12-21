package io.defitrack.protocol.compound

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class CompoundAddressesProvider {
    val CONFIG = mapOf(
        Network.ETHEREUM to CompoundAddresses(
            v2Controller = "0x3d9819210a31b4961b30ef54be2aed79b9c9cd3b",
            v3Tokens = listOf(
                "0xc3d688B66703497DAA19211EEdff47f25384cdc3",
                "0xa17581a9e3356d9a858b789d68b4d866e593ae94",
            ),
            rewards = "0x1B0e765F6224C21223AeA2af16c1C46E38885a40"
        ),
        Network.POLYGON to CompoundAddresses(
            v3Tokens = listOf(
                "0xF25212E676D1F7F89Cd72fFEe66158f541246445",
            ),
            rewards = "0x45939657d1CA34A8FA39A924B71D28Fe8431e581"
        ),
        Network.ARBITRUM to CompoundAddresses(
            v3Tokens = listOf(
                "0xA5EDBDD9646f8dFF606d7448e414884C7d905dCA",
                "0x9c4ec768c28520B50860ea7a15bd7213a9fF58bf",
            ),
            rewards = "0x88730d254A2f7e6AC8388c3198aFd694bA9f7fae"
        ),
        Network.BASE to CompoundAddresses(
            v3Tokens = listOf(
                "0x9c4ec768c28520B50860ea7a15bd7213a9fF58bf",
                "0x46e6b214b524310239732D51387075E0e70970bf"
            ),
            rewards = "0x123964802e6ABabBE1Bc9547D72Ef1B69B00A6b1"
        )
    )

    fun getV3Tokens(network: Network): List<String> {
        return CONFIG[network]?.v3Tokens ?: emptyList()
    }

    fun getRewards(network: Network): String? {
        return CONFIG[network]?.rewards
    }
}

data class CompoundAddresses(
    val v2Controller: String? = null,
    val v3Tokens: List<String> = emptyList(),
    val rewards: String
)