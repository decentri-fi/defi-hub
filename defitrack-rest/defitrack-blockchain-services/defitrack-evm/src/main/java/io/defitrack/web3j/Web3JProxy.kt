package io.defitrack.web3j

import io.defitrack.evm.EvmContractInteractionCommand
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.multicall.MulticallService
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthBlock
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt
import org.web3j.protocol.core.methods.response.EthLog
import org.web3j.protocol.core.methods.response.EthTransaction
import org.web3j.protocol.exceptions.ClientConnectionException
import java.math.BigInteger

@Component
class Web3JProxy(
    private val primaryWeb3j: Web3j,
    @Qualifier("fallbackWeb3js") private val fallbacks: List<Web3j>,
    private val multicallService: MulticallService,
    private val observationRegistry: ObservationRegistry
) {

    suspend fun call(calls: List<EvmContractInteractionCommand>): List<EthCall> {
        return multicallService.call(calls) { call(it) }
    }

    suspend fun getLogs(
        getEventLogsCommand: GetEventLogsCommand,
        _web3j: Web3j = primaryWeb3j
    ): EthLog {
        val ethFilter = with(
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
        val result = _web3j.ethGetLogs(ethFilter).send()
        return if (result.hasError() && result.error.code == 429) {
            delay(1000)
            getLogs(getEventLogsCommand)
        } else if (result.hasError()) {
            anyFallback()?.let {
                getLogs(getEventLogsCommand, it)
            } ?: result
        } else {
            result
        }
    }

    suspend fun getBlockByHash(hash: String, _web3j: Web3j = primaryWeb3j): EthBlock? {
        return try {
            val send = _web3j.ethGetBlockByHash(hash, false).send()
            if (send.hasError() && send.error.code == 429) {
                delay(100)
                getBlockByHash(hash)
            } else if (send.hasError()) {
                anyFallback()?.let {
                    getBlockByHash(hash, fallbacks.shuffled().first())
                }
            } else {
                send
            }
        } catch (ex: Exception) {
            getBlockByHash(hash, anyFallback(true)!!)
        }
    }

    suspend fun ethGetBalance(
        address: String,
        _web3j: Web3j = primaryWeb3j
    ): BigInteger = withContext(Dispatchers.IO) {
        try {
            val send = _web3j.ethGetBalance(address, DefaultBlockParameterName.PENDING).send()
            if (send.hasError() && send.error.code == 429) {
                delay(100)
                ethGetBalance(address)
            } else if (send.hasError()) {
                anyFallback()?.let {
                    ethGetBalance(address, fallbacks.shuffled().first())
                } ?: send.balance
            } else {
                send.balance
            }
        } catch (ex: Exception) {
            anyFallback()?.let {
                ethGetBalance(address, it)
            } ?: throw ex
        }
    }

    suspend fun getTransactionByHash(txId: String, _web3j: Web3j = primaryWeb3j): EthTransaction {
        val observation = Observation.start("transaction-receipt", observationRegistry)
        return observation.openScope().use {
            try {
                val result = _web3j.ethGetTransactionByHash(txId).send()
                if (result.hasError() && result.error.code == 429) {
                    observation.event(Observation.Event.of("429"))
                    delay(100L)
                    getTransactionByHash(txId)
                } else {
                    observation.event(Observation.Event.of("success"))
                    result
                }
            } catch (ex: Exception) {
                if (ex.message?.contains("429") == true) {
                    observation.event(Observation.Event.of("endpoint.throttled", "thirdparty.endpoint.throttled"))
                    delay(100L)
                    return getTransactionByHash(txId, anyFallback(true)!!)

                } else if (ex.message?.contains("limit exceeded") == true) {
                    observation.event(
                        Observation.Event.of(
                            "endpoint.capacity-exceeded",
                            "thirdparty.monthly-capacity-limit-exceeded"
                        )
                    )
                    anyFallback()?.let {
                        getTransactionByHash(txId, it)
                    } ?: throw ex
                } else {
                    throw ex
                }
            }
        }
    }


    suspend fun getTransactionReceipt(txId: String, _web3j: Web3j = primaryWeb3j): EthGetTransactionReceipt {
        val observation = Observation.start("transaction-receipt", observationRegistry)
        return observation.openScope().use {
            try {
                val result = _web3j.ethGetTransactionReceipt(txId).send()
                if (result.hasError() && result.error.code == 429) {
                    observation.event(Observation.Event.of("429"))
                    delay(100L)
                    getTransactionReceipt(txId)
                } else {
                    observation.event(Observation.Event.of("success"))
                    result
                }
            } catch (ex: Exception) {
                if (ex.message?.contains("429") == true) {
                    observation.event(Observation.Event.of("endpoint.throttled", "thirdparty.endpoint.throttled"))
                    delay(100L)
                    return getTransactionReceipt(txId, anyFallback(true)!!)

                } else if (ex.message?.contains("limit exceeded") == true) {
                    observation.event(
                        Observation.Event.of(
                            "endpoint.capacity-exceeded",
                            "thirdparty.monthly-capacity-limit-exceeded"
                        )
                    )
                    anyFallback()?.let {
                        getTransactionReceipt(txId, it)
                    } ?: throw ex
                } else {
                    throw ex
                }
            }
        }
    }

    suspend fun call(
        evmContractInteractionCommand: EvmContractInteractionCommand,
        _web3j: Web3j = primaryWeb3j
    ): EthCall {
        return withContext(Dispatchers.IO) {
            with(evmContractInteractionCommand) {
                val observation = Observation.start("contract-interaction", observationRegistry)
                observation.openScope().use {
                    try {
                        val result = ethCall(evmContractInteractionCommand, _web3j)

                        if (result.hasError() && result.error.code == 429) {
                            observation.event(Observation.Event.of("429"))
                            delay(100L)
                            call(evmContractInteractionCommand)
                        } else {
                            observation.event(Observation.Event.of("success"))
                            result
                        }
                    } catch (ex: ClientConnectionException) {
                        if (ex.message?.contains("429") == true) {
                            observation.event(
                                Observation.Event.of(
                                    "endpoint.throttled",
                                    "thirdparty.endpoint.throttled"
                                )
                            )
                            delay(100L)
                            return@with call(evmContractInteractionCommand)

                        } else if (ex.message?.contains("limit exceeded") == true) {
                            observation.event(
                                Observation.Event.of(
                                    "endpoint.capacity-exceeded",
                                    "thirdparty.monthly-capacity-limit-exceeded"
                                )
                            )
                            anyFallback()?.let {
                                call(evmContractInteractionCommand, it)
                            } ?: throw ex
                        } else {
                            throw ex
                        }
                    }
                }.also {
                    observation.stop()
                }
            }
        }
    }

    private fun anyFallback(includesDefault: Boolean = false): Web3j? {
        return fallbacks.shuffled().firstOrNull() ?: if (includesDefault) primaryWeb3j else null
    }

    private fun ethCall(evmContractInteractionCommand: EvmContractInteractionCommand, web3j: Web3j): EthCall =
        web3j.ethCall(
            Transaction.createEthCallTransaction(
                evmContractInteractionCommand.from,
                evmContractInteractionCommand.contract,
                evmContractInteractionCommand.function
            ), DefaultBlockParameterName.PENDING
        ).send()
}