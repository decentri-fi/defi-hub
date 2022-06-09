package io.defitrack.protocol.domain

import io.defitrack.protocol.DefiPrimitive
import io.defitrack.protocol.Protocol


class ProtocolVO(
    val name: String,
    val logo: String,
    val slug: String,
    val primitives: List<DefiPrimitive>,
    val website: String
)

fun Protocol.toVO(): ProtocolVO {
    return ProtocolVO(
        name = this.name,
        logo = this.getImage(),
        slug = this.slug,
        primitives = this.primitives,
        website = this.website
    )
}