package io.defitrack.network

import io.defitrack.common.network.Network

class NetworkInformationVO(
    val name: String,
    val logo: String,
    val chainId: Int
) {
    fun toNetwork(): Network {
        return Network.fromString(name)
    }
}


fun Network.toVO(): NetworkInformationVO {
    return NetworkInformationVO(
        name = this.name,
        logo = this.logo,
        chainId = this.chainId
    )
}