package io.defitrack

import io.defitrack.common.network.Network
import io.defitrack.network.NetworkVO
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ProtocolVO
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MetaRestController(
    private val protocolVOMapper: ProtocolVOMapper
) {

    @GetMapping("/protocols")
    fun getProtocols(): List<ProtocolVO> {
        return Protocol.entries.map(protocolVOMapper::map)
    }

    @GetMapping("/networks")
    fun getNetworks(): List<NetworkVO> {
        return Network.entries.map(Network::toVO)
    }
}