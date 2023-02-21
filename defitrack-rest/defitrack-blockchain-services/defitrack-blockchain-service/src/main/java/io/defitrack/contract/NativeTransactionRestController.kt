package io.defitrack.contract

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.EthLog
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/events")
class NativeTransactionRestController(
    private val web3j: Web3j
) {

    @PostMapping("/logs")
    fun getEvents(@RequestBody getEventLogsCommand: GetEventLogsCommand): CompletableFuture<EthLog> {
        val ethFilter =
            with(EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                getEventLogsCommand.address
            )) {
                addSingleTopic(getEventLogsCommand.topic)
                getEventLogsCommand.optionalTopics.forEach {
                    if(it != null) {
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
        val address: String,
        val topic: String,
        val optionalTopics: List<String?> = emptyList()
    )
}