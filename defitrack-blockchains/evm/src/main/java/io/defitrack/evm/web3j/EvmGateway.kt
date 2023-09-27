package io.defitrack.evm.web3j

import io.defitrack.evm.contract.EvmContractInteractionCommand
import io.defitrack.evm.contract.GetEventLogsCommand
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.core.methods.response.EthLog
import org.web3j.protocol.exceptions.ClientConnectionException
import java.math.BigInteger

@Component
@ConditionalOnBean(Web3j::class)
class EvmGateway(
    private val primaryWeb3j: Web3j,
    @Qualifier("fallbackWeb3js") private val fallbacks: List<Web3j>,
    private val observationRegistry: ObservationRegistry
) {

    suspend fun getLogs(getEventLogsCommand: GetEventLogsCommand,
                        _web3j: Web3j = primaryWeb3j): EthLog {
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
        val result =  _web3j.ethGetLogs(ethFilter).send()
        return if (result.hasError() && result.error.code == 429) {
            delay(1000)
            getLogs(getEventLogsCommand)
        } else if (result.hasError()) {
            fallbacks.shuffled().firstOrNull()?.let {
                getLogs(getEventLogsCommand, fallbacks.shuffled().first())
            } ?: result
        } else {
            result
        }
    }

    suspend fun ethGetBalance(
        address: String,
        _web3j: Web3j = primaryWeb3j
    ): BigInteger = withContext(Dispatchers.IO) {
        val send = _web3j.ethGetBalance(address, DefaultBlockParameterName.PENDING).send()
        if (send.hasError() && send.error.code == 429) {
            delay(1000)
            ethGetBalance(address)
        } else if (send.hasError()) {
            fallbacks.shuffled().firstOrNull()?.let {
                ethGetBalance(address, fallbacks.shuffled().first())
            } ?: send.balance
        } else {
            send.balance
        }
    }

    suspend fun call(
        evmContractInteractionCommand: EvmContractInteractionCommand,
        _web3j: Web3j = primaryWeb3j
    ): EthCall {
        return withContext(Dispatchers.IO) {
            with(evmContractInteractionCommand) {
                val observation = Observation.createNotStarted("contract-interaction", observationRegistry)
                try {
                    val result = ethCall(evmContractInteractionCommand, _web3j)

                    if (result.hasError() && result.error.code == 429) {
                        observation.event(Observation.Event.of("429"))
                        delay(1000L)
                        call(evmContractInteractionCommand)
                    } else if (result.hasError()) {
                        fallbacks.shuffled().firstOrNull()?.let {
                            call(evmContractInteractionCommand, fallbacks.shuffled().first())
                        } ?: result
                    } else {
                        result
                    }
                } catch (ex: ClientConnectionException) {
                    if (ex.message?.contains("429") == true) {
                        observation.event(Observation.Event.of("429"))
                        delay(1000L)
                        return@with call(evmContractInteractionCommand)
                    } else {
                        throw ex
                    }
                } finally {
                    observation.stop()
                }
            }
        }
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