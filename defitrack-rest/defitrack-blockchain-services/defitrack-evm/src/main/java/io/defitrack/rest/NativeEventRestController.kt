package io.defitrack.rest

import io.defitrack.web3j.Web3JProxy
import io.defitrack.evm.GetEventLogsCommand
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.core.methods.response.EthLog

@RestController
@RequestMapping("/events")
class NativeEventRestController(
    private val web3JProxy: Web3JProxy
) {

    @PostMapping("/logs")
    suspend fun getEvents(@RequestBody getEventLogsCommand: GetEventLogsCommand): EthLog {
        require(getEventLogsCommand.addresses.isNotEmpty()) { "Address must not be empty" }
        return web3JProxy.getLogs(getEventLogsCommand)
    }
}