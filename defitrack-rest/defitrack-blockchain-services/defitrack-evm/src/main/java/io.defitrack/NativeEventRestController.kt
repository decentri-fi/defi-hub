package io.defitrack

import io.defitrack.evm.contract.GetEventLogsCommand
import io.defitrack.evm.web3j.EvmGateway
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.core.methods.response.EthLog

@RestController
@RequestMapping("/events")
class NativeEventRestController(
    private val evmGateway: EvmGateway
) {

    @PostMapping("/logs")
    suspend fun getEvents(@RequestBody getEventLogsCommand: GetEventLogsCommand): EthLog {
        require(getEventLogsCommand.addresses.isNotEmpty()) { "Address must not be empty" }
        return evmGateway.getLogs(getEventLogsCommand)
    }
}