package io.defitrack.protocol.domain

import io.defitrack.protocol.Protocol


class ProtocolVO(
    val name: String,
    val logo: String,
    val slug: String,
)

fun Protocol.toVO(): ProtocolVO {
    return ProtocolVO(
        name = this.name,
        logo = this.getImage(),
        slug = this.slug,
    )
}