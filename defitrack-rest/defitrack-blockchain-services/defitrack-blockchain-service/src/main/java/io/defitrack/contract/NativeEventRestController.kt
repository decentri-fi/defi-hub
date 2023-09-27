package io.defitrack.contract

import io.defitrack.evm.contract.GetEventLogsCommand
import io.defitrack.evm.web3j.EvmGateway
import io.micrometer.core.annotation.Timed
import kotlinx.coroutines.delay
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.EthLog
import java.math.BigInteger

@RestController
@RequestMapping("/events")
class NativeEventRestController(
    private val evmGateway: EvmGateway
) {

    @PostMapping("/logs")
    @Timed("blockchain.events.logs")
    suspend fun getEvents(@RequestBody getEventLogsCommand: GetEventLogsCommand): EthLog {
        require(getEventLogsCommand.addresses.isNotEmpty()) { "Address must not be empty" }
        return evmGateway.getLogs(getEventLogsCommand)
    }
}