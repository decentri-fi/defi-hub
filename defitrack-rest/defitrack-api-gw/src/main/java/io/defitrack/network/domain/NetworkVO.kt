package io.defitrack.network.domain

import io.defitrack.common.network.Network


class NetworkVO(
    val name: String,
    val logo: String,
    val slug: String,
)

fun Network.toVO(): NetworkVO {
    return NetworkVO(
        name = this.name,
        logo = this.getImage(),
        slug = this.slug,
    )
}