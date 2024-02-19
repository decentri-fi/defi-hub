package io.defitrack.web3j

import io.defitrack.config.Web3JEndpoints
import io.defitrack.evm.EvmContractInteractionCommand
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.multicall.MulticallService
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
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
import java.util.regex.Matcher
import java.util.regex.Pattern

@Component
class Web3JProxy(
    private val web3JEndpoints: Web3JEndpoints,
    private val multicallService: MulticallService,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun call(calls: List<EvmContractInteractionCommand>, _web3j: Web3j): List<EthCall> {
        return multicallService.call(calls) { call(it, _web3j) }
    }

    data class RunWithFallbackContext<T : Response<*>>(
        val requestProvider: (web3j: Web3j) -> Request<*, T>,
        val _web3j: Web3j,
        val maxTries: Int = 3,
        val tries: Int = 0,
    ) {
        fun increment(): RunWithFallbackContext<T> {
            return this.copy(tries = this.tries + 1)
        }
    }


    suspend fun getLogs(
        getEventLogsCommand: GetEventLogsCommand,
        _web3j: Web3j
    ): EthLog {
        val ethFilter = getEthFilter(getEventLogsCommand)

        return try {
            val log = runWithFallback(
                RunWithFallbackContext(
                    {
                        _web3j.ethGetLogs(ethFilter)
                    },
                    _web3j
                )
            )

            when {
                log.exceedsSize() -> {
                    val splitMatcher = log.exceededPattern.matcher(log.error.message)
                    if (splitMatcher.find()) {
                        fromSplitMatcher(splitMatcher, getEventLogsCommand, _web3j)
                    } else {
                        log
                    }
                }

                log.exceedsBlockLimit() -> {
                    split(getEventLogsCommand, _web3j)
                }

                else -> {
                    log
                }
            }
        } catch (needssplit: NeedsSplitException) {
            split(getEventLogsCommand, _web3j)
        } catch (ex: Exception) {
            throw ex
        }
    }

    private suspend fun split(getEventLogsCommand: GetEventLogsCommand, _web3j: Web3j): EthLog {
        val startBlock = getEventLogsCommand.fromBlock ?: BigInteger.ZERO
        val endBlock = startBlock + BigInteger.valueOf(10000)
        logger.info("too many results, splitting into two calls: {} - {}", startBlock, endBlock)
        return listOf(
            getLogs(
                GetEventLogsCommand(
                    getEventLogsCommand.addresses,
                    getEventLogsCommand.topic,
                    getEventLogsCommand.optionalTopics,
                    startBlock,
                    endBlock
                ),
                _web3j
            ),
            getLogs(
                GetEventLogsCommand(
                    getEventLogsCommand.addresses,
                    getEventLogsCommand.topic,
                    getEventLogsCommand.optionalTopics,
                    endBlock.plus(BigInteger.ONE),
                    getEventLogsCommand.toBlock
                ),
                _web3j
            )
        ).reduce { acc, ethLog ->
            return EthLog().apply {
                this.result = acc.logs + ethLog.logs
                this.error = if (acc.hasError()) acc.error else ethLog.error
            }
        }
    }

    private suspend fun fromSplitMatcher(
        matcher: Matcher,
        getEventLogsCommand: GetEventLogsCommand,
        _web3j: Web3j
    ): EthLog {
        val start = matcher.group(1).removePrefix("0x")
        val end = matcher.group(2).removePrefix("0x")
        val startBlock = BigInteger(start, 16)
        val endBlock = BigInteger(end, 16)
        logger.info("too many results, splitting into two calls: {} - {}", startBlock, endBlock)
        return listOf(
            getLogs(
                GetEventLogsCommand(
                    getEventLogsCommand.addresses,
                    getEventLogsCommand.topic,
                    getEventLogsCommand.optionalTopics,
                    startBlock,
                    endBlock
                ),
                _web3j
            ),
            getLogs(
                GetEventLogsCommand(
                    getEventLogsCommand.addresses,
                    getEventLogsCommand.topic,
                    getEventLogsCommand.optionalTopics,
                    endBlock.plus(BigInteger.ONE),
                    getEventLogsCommand.toBlock
                ),
                _web3j
            )
        ).reduce { acc, ethLog ->
            return EthLog().apply {
                this.result = acc.logs + ethLog.logs
                this.error = if (acc.hasError()) acc.error else ethLog.error
            }
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
        //regex: "Log response size exceeded.[\\s\\S]*?range should work: \\[([\\w]*?), ([\\w]*?)\\]"
        return this.hasError() && exceededPattern.matcher(this.error.message).matches()
    }

    fun EthLog.exceedsBlockLimit(): Boolean {
        return this.hasError() && this.error.message.contains("logs are limited to a 10000 block range")
    }

    suspend fun getBlockByHash(hash: String, _web3j: Web3j): EthBlock? {
        return runWithFallback(
            RunWithFallbackContext(
                {
                    it.ethGetBlockByHash(hash, false)
                }, _web3j
            )
        )
    }

    suspend fun ethGetBalance(
        address: String,
        _web3j: Web3j
    ): EthGetBalance {
        return runWithFallback(
            RunWithFallbackContext(
                {
                    it.ethGetBalance(address, DefaultBlockParameterName.PENDING)
                },
                _web3j
            )
        )
    }

    suspend fun getTransactionByHash(
        txId: String,
        _web3j: Web3j
    ): EthTransaction {
        return runWithFallback(
            RunWithFallbackContext(
                {
                    it.ethGetTransactionByHash(txId)
                },
                _web3j
            )
        )
    }


    suspend fun getTransactionReceipt(
        txId: String,
        _web3j: Web3j
    ): EthGetTransactionReceipt {
        return runWithFallback(
            RunWithFallbackContext(
                { it.ethGetTransactionReceipt(txId) }, _web3j
            )
        )
    }

    suspend fun call(
        evmContractInteractionCommand: EvmContractInteractionCommand,
        _web3j: Web3j
    ): EthCall {
        return runWithFallback(
            RunWithFallbackContext(
                {
                    ethCall(evmContractInteractionCommand, it)
                },
                _web3j
            )
        )
    }

    suspend fun <T : Response<*>> runWithFallback(previousContext: RunWithFallbackContext<T>): T {

        val runWithFallbackContext = previousContext.increment()
        if (runWithFallbackContext.tries > runWithFallbackContext.maxTries) {
            logger.info("exhausted retries, returning exception")
            throw ExchaustedRetriesException()
        }

        logger.info("invoking with web3j: {}", runWithFallbackContext._web3j)
        val request = runWithFallbackContext.requestProvider.invoke(runWithFallbackContext._web3j)

        return try {
            val result = request.send()

            if (result.hasError()) {
                handleException(result.error.message, runWithFallbackContext)
            } else {
                result
            }
        } catch (needsSplitException: NeedsSplitException) {
            throw needsSplitException
        } catch (ex: Exception) {
            logger.info("got exception using web3j {}", runWithFallbackContext._web3j)
            handleException(ex.message ?: "", runWithFallbackContext)
        }
    }

    private suspend fun <T : Response<*>> handleException(
        message: String,
        runWithFallbackContext: RunWithFallbackContext<T>,
    ): T {
        return if (message.contains("429")) {
            logger.info("throttled, waiting and running with fallback")
            delay(100L)
            runWithFallback(
                runWithFallbackContext.copy(
                    _web3j = web3JEndpoints.getFallback(true)!!
                )
            )
        } else if (message.contains("limit exceeded")) {
            logger.info("capacity exceeded, running with fallback")
            web3JEndpoints.observePrimaryDown()
            web3JEndpoints.getFallback()?.let {
                runWithFallback(runWithFallbackContext.copy(_web3j = it))
            } ?: throw RuntimeException("unable to find working fallback web3j: $message")
        } else if (message.contains("logs are limited to a 10000 block range")) {
            throw NeedsSplitException()
        } else {
            throw RuntimeException("unable to find working fallback web3j: $message")
        }
    }

    class NeedsSplitException : RuntimeException()
    class ExchaustedRetriesException : RuntimeException()

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