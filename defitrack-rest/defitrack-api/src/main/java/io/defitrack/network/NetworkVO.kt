package io.defitrack.network

import io.defitrack.common.network.Network
import io.defitrack.network.NetworkVO.Companion.IMAGE_BASE_PATH


class NetworkVO(
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

fun Network.toVO(): NetworkVO {

    fun getImage(): String = IMAGE_BASE_PATH + logo

    return NetworkVO(
        name = this.name,
        logo = getImage(),
        chainId = this.chainId
    )
}
