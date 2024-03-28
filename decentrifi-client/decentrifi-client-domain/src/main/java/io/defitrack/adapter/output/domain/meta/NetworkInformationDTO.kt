package io.defitrack.adapter.output.domain.meta

import io.defitrack.common.network.Network

val IMAGE_BASE_PATH = "https://github.com/decentri-fi/data/raw/master/logo/network/"

class NetworkInformationDTO(
    val name: String,
    val logo: String,
    val chainId: Int
) {

    fun toNetwork(): Network {
        return Network.valueOf(name)
    }
}
