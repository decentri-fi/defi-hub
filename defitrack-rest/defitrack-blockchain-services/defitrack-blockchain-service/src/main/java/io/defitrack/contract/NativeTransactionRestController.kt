package io.defitrack.contract

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
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/events")
class NativeTransactionRestController(
    private val web3j: Web3j
) {

    @PostMapping("/logs")
    fun getEvents(@RequestBody getEventLogsCommand: GetEventLogsCommand): CompletableFuture<EthLog> {
        require(getEventLogsCommand.addresses.isNotEmpty()) { "Address must not be empty" }
        val ethFilter =
            with(
                EthFilter(
                    getEventLogsCommand.fromBlock?.let {
                        DefaultBlockParameterNumber(BigInteger(it, 10))
                    } ?: DefaultBlockParameterName.EARLIEST,
                    getEventLogsCommand.toBlock?.let {
                        DefaultBlockParameterNumber(BigInteger(it, 10))
                    } ?: DefaultBlockParameterName.LATEST,
                    getEventLogsCommand.addresses
                )
            ) {
                addSingleTopic(getEventLogsCommand.topic)
                getEventLogsCommand.optionalTopics.forEach {
                    if (it != null) {
                        addOptionalTopics(it)
                    } else {
                        addNullTopic()
                    }
                }
                this
            }
        return web3j.ethGetLogs(ethFilter).sendAsync()
    }

    class GetEventLogsCommand(
        val addresses: List<String>,
        val topic: String,
        val optionalTopics: List<String?> = emptyList(),
        val fromBlock: String? = null,
        val toBlock: String? = null
    )
}