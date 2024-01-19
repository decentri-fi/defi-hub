package io.defitrack.adapter.output.resource

import io.defitrack.domain.NetworkInformation


internal class NetworkInformationResponse(
    val name: String,
    val logo: String,
    val chainId: Int
){
    fun toNetworkInformation(): NetworkInformation {
        return NetworkInformation(
            name = name,
            logo = logo,
            chainId = chainId
        )
    }
}

