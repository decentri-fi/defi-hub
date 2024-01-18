package io.defitrack.network

import io.defitrack.common.network.Network
import io.defitrack.network.NetworkInformation.Companion.IMAGE_BASE_PATH


class NetworkInformation(
    val name: String,
    val logo: String,
    val chainId: Int
) {

    companion object {
        val IMAGE_BASE_PATH = "https://github.com/decentri-fi/data/raw/master/logo/network/"
    }

    fun toNetwork(): Network {
        return Network.valueOf(name)
    }
}

fun Network.toVO(): NetworkInformation {

    fun getImage(): String = IMAGE_BASE_PATH + logo

    return NetworkInformation(
        name = this.name,
        logo = getImage(),
        chainId = this.chainId
    )
}
