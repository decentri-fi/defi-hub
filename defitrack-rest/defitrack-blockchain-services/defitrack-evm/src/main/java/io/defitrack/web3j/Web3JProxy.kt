package io.defitrack.web3j

import io.defitrack.evm.EvmContractInteractionCommand
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.multicall.MulticallService
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.delay
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
        return runWithFallback({
            _web3j.ethGetLogs(ethFilter)
        })
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