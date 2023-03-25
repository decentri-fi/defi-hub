package io.defitrack.protocol

import org.springframework.hateoas.RepresentationModel


class ProtocolVO(
    val name: String,
    val logo: String,
    val slug: String,
    val primitives: List<DefiPrimitive>,
    val website: String
) : RepresentationModel<ProtocolVO>()

fun Protocol.toVO(): ProtocolVO {
    return ProtocolVO(
        name = this.name,
        logo = this.getImage(),
        slug = this.slug,
        primitives = this.primitives,
        website = this.website
    )
}