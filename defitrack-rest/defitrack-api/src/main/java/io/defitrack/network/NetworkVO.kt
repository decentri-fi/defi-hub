package io.defitrack.network

import io.defitrack.common.network.Network


class NetworkVO(
    val name: String,
    val logo: String,
    val chainId: Int,
)

fun Network.toVO(): NetworkVO {
    return NetworkVO(
        name = this.name,
        logo = this.getImage(),
        chainId = this.chainId
    )
}