package io.defitrack

import io.defitrack.common.network.Network
import io.defitrack.network.NetworkVO
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ProtocolVO
import io.defitrack.protocol.mapper.ProtocolVOMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(
    name = "Metadata",
    description = "Metadata API. Everything related to the metadata of the API. (networks, protocols...)"
)
class MetaRestController(
    private val protocolVOMapper: ProtocolVOMapper
) {

    @GetMapping("/protocols")
    @Operation(summary = "Get all supported protocols")
    fun getProtocols(): List<ProtocolVO> {
        return Protocol.entries.map(protocolVOMapper::map)
    }

    @GetMapping("/networks")
    @Operation(summary = "Get all supported network")
    fun getNetworks(): List<NetworkVO> {
        return Network.entries.map(Network::toVO)
    }
}