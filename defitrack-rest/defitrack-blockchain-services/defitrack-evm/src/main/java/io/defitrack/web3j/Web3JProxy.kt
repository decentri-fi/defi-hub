package io.defitrack.web3j

import io.defitrack.evm.EvmContractInteractionCommand
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.multicall.MulticallService
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.*
import java.math.BigInteger
import java.util.regex.Pattern

@Component
class Web3JProxy(
    private val primaryWeb3j: Web3j,
    @Qualifier("fallbackWeb3js") private val fallbacks: List<Web3j>,
    private val multicallService: MulticallService,
    private val observationRegistry: ObservationRegistry
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun call(calls: List<EvmContractInteractionCommand>): List<EthCall> {
        return multicallService.call(calls) { call(it) }
    }

    suspend fun getLogs(
        getEventLogsCommand: GetEventLogsCommand,
        _web3j: Web3j = primaryWeb3j
    ): EthLog {
        val ethFilter = getEthFilter(getEventLogsCommand)
        val log = runWithFallback({
            _web3j.ethGetLogs(ethFilter)
        })

        return if (log.exceedsSize()) {
            //regex: "Log response size exceeded.[\\s\\S]*?range should work: \\[([\\w]*?), ([\\w]*?)\\]"
            val matcher = log.exceededPattern.matcher(log.error.message)
            if (matcher.find()) {
                val start = matcher.group(1).removePrefix("0x")
                val end = matcher.group(2).removePrefix("0x")
                val startBlock = BigInteger(start, 16)
                val endBlock = BigInteger(end, 16)
                logger.info("too many results, splitting into two calls: {} - {}", startBlock, endBlock)
                listOf(
                    getLogs(
                        GetEventLogsCommand(
                            getEventLogsCommand.addresses,
                            getEventLogsCommand.topic,
                            getEventLogsCommand.optionalTopics,
                            startBlock,
                            endBlock
                        )
                    ),
                    getLogs(
                        GetEventLogsCommand(
                            getEventLogsCommand.addresses,
                            getEventLogsCommand.topic,
                            getEventLogsCommand.optionalTopics,
                            endBlock.plus(BigInteger.ONE),
                            getEventLogsCommand.toBlock
                        )
                    )
                ).reduce { acc, ethLog ->
                    return EthLog().apply {
                        this.result = acc.logs + ethLog.logs
                        this.error = if (acc.hasError()) acc.error else ethLog.error
                    }
                }
            } else {
                log
            }
        } else {
            log
        }
    }

    private fun getEthFilter(getEventLogsCommand: GetEventLogsCommand): EthFilter {
        return with(
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
    }

    val EthLog.exceededPattern: Pattern
        get() {
            val regex = "Log response size exceeded.[\\s\\S]*?range should work: \\[([\\w]*?), ([\\w]*?)\\]"
            return Pattern.compile(regex)
        }

    fun EthLog.exceedsSize(): Boolean {
        return this.hasError() && exceededPattern.matcher(this.error.message).matches()
    }

    suspend fun getBlockByHash(hash: String, _web3j: Web3j = primaryWeb3j): EthBlock? {
        return runWithFallback(
            {
                it.ethGetBlockByHash(hash, false)
            }
        )
    }

    suspend fun ethGetBalance(
        address: String,
        _web3j: Web3j = primaryWeb3j
    ): EthGetBalance {
        return runWithFallback(
            {
                it.ethGetBalance(address, DefaultBlockParameterName.PENDING)
            }
        )
    }

    suspend fun getTransactionByHash(txId: String, _web3j: Web3j = primaryWeb3j): EthTransaction {
        return runWithFallback(
            {
                it.ethGetTransactionByHash(txId)
            }
        )
    }


    suspend fun getTransactionReceipt(txId: String, _web3j: Web3j = primaryWeb3j): EthGetTransactionReceipt {
        return runWithFallback({ it.ethGetTransactionReceipt(txId) })
    }

    suspend fun call(
        evmContractInteractionCommand: EvmContractInteractionCommand,
    ): EthCall {
        return runWithFallback(
            {
                ethCall(evmContractInteractionCommand, it)
            }, primaryWeb3j
        )
    }

    suspend fun <T : Response<*>> runWithFallback(
        requestProvider: (web3j: Web3j) -> Request<*, T>,
        _web3j: Web3j = primaryWeb3j
    ): T {
        val request = requestProvider.invoke(_web3j)

        val observation = Observation.start("web3j-" + request.method, observationRegistry)
        return observation.openScope().use {
            try {
                val result = request.send()

                if (result.hasError() && result.error.code == 429) {
                    observation.event(Observation.Event.of("429"))
                    delay(100L)
                    runWithFallback(requestProvider, anyFallback(true)!!)
                } else {
                    observation.event(Observation.Event.of("success"))
                    result
                }
            } catch (ex: Exception) {
                if (ex.message?.contains("429") == true) {
                    observation.event(
                        Observation.Event.of(
                            "endpoint.throttled",
                            "thirdparty.endpoint.throttled"
                        )
                    )
                    delay(100L)
                    return runWithFallback(requestProvider, anyFallback(true)!!)
                } else if (ex.message?.contains("limit exceeded") == true) {
                    observation.event(
                        Observation.Event.of(
                            "endpoint.capacity-exceeded",
                            "thirdparty.monthly-capacity-limit-exceeded"
                        )
                    )
                    anyFallback()?.let {
                        runWithFallback(requestProvider, it)
                    } ?: throw ex
                } else {
                    throw ex
                }
            }
        }
    }

    private fun anyFallback(includesDefault: Boolean = false): Web3j? {
        return fallbacks.shuffled().firstOrNull() ?: if (includesDefault) primaryWeb3j else null
    }

    private fun ethCall(
        evmContractInteractionCommand: EvmContractInteractionCommand,
        web3j: Web3j
    ): Request<*, EthCall> =
        web3j.ethCall(
            Transaction.createEthCallTransaction(
                evmContractInteractionCommand.from,
                evmContractInteractionCommand.contract,
                evmContractInteractionCommand.function
            ), evmContractInteractionCommand.block?.let {
                DefaultBlockParameterNumber(it)
            } ?: DefaultBlockParameterName.PENDING
        )
}