package io.codechef.defitrack.protocol

import io.defitrack.protocol.Protocol


class ProtocolVO(
    val name: String,
    val logo: String,
    val slug: String,
    val dedicatedMicroService: Boolean
)

fun Protocol.toVO(): ProtocolVO {
    return ProtocolVO(
        name = this.name,
        logo = this.getImage(),
        slug = this.slug,
        dedicatedMicroService = this.dedicatedMicroService
    )
}