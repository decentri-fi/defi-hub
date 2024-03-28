package io.defitrack.event.event

import io.defitrack.common.network.Network

val IMAGE_BASE_PATH = "https://github.com/decentri-fi/data/raw/master/logo/network/"

class NetworkInformation(
    val name: String,
    val logo: String,
    val chainId: Int
)

fun Network.toNetworkInformation(): NetworkInformation {

    fun getImage(): String = IMAGE_BASE_PATH + logo

    return NetworkInformation(
        name = this.name,
        logo = getImage(),
        chainId = this.chainId
    )
}
