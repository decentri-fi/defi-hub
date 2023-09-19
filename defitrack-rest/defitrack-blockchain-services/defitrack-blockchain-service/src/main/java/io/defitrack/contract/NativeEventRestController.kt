package io.defitrack.contract

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
    private val web3j: Web3j
) {

    @PostMapping("/logs")
    @Timed("blockchain.events.logs")
    suspend fun getEvents(@RequestBody getEventLogsCommand: GetEventLogsCommand): EthLog {
        require(getEventLogsCommand.addresses.isNotEmpty()) { "Address must not be empty" }
        val ethFilter =
            with(
                EthFilter(
                    getEventLogsCommand.fromBlock?.let {
                        DefaultBlockParameterNumber(it)
                    } ?: DefaultBlockParameterName.EARLIEST,
                    getEventLogsCommand.toBlock?.let {
                        DefaultBlockParameterNumber(it)
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
        val result =  web3j.ethGetLogs(ethFilter).send()
        return if (result.hasError() && result.error.code == 429) {
            delay(1000)
            getEvents(getEventLogsCommand)
        }
        else {
            result
        }
    }

    class GetEventLogsCommand(
        val addresses: List<String>,
        val topic: String,
        val optionalTopics: List<String?> = emptyList(),
        val fromBlock: BigInteger? = null,
        val toBlock: BigInteger? = null
    )
}