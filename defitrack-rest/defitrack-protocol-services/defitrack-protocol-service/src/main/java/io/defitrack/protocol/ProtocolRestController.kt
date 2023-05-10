package io.defitrack.protocol

import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class ProtocolRestController(
    private val protocolProvider: ProtocolProvider,
    private val protocolVOMapper: ProtocolVOMapper
) {

    @GetMapping
    fun getProtocol(): ProtocolVO {
        return protocolVOMapper.map(protocolProvider.getProtocol())
    }
}