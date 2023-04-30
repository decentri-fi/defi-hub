package io.defitrack.protocol

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class ProtocolRestController(
    private val protocolProvider: ProtocolProvider
) {

    @GetMapping
    fun getProtocol(): ProtocolVO {
        return protocolProvider.getProtocol().toVO()
    }
}

fun Protocol.toVO(): ProtocolVO {
    return ProtocolVO(
        name = this.name,
        logo = this.getImage(),
        slug = this.slug,
        primitives = this.primitives,
        website = this.website
    )
}