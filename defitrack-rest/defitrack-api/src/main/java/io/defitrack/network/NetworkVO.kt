package io.defitrack.network

import io.defitrack.common.network.Network


class NetworkVO(
    val name: String,
    val logo: String,
    val chainId: Int
) {
    fun toNetwork(): Network {
        return Network.valueOf(name)
    }
}

fun Network.toVO(): NetworkVO {
    return NetworkVO(
        name = this.name,
        logo = this.getImage(),
        chainId = this.chainId
    )
}
