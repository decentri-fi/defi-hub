package io.defitrack.evm.contract

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import com.google.gson.JsonParser
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.EvmContractInteractionCommand
import io.defitrack.evm.GetEventLogsCommand
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.core.methods.response.EthLog
import org.web3j.protocol.core.methods.response.EthLog.LogObject
import org.web3j.protocol.core.methods.response.Log
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Collections.emptyList
import org.web3j.abi.datatypes.Function as Web3Function


class BlockchainGateway(
    val network: Network,
    val multicallCaller: MultiCallCaller,
    val httpClient: HttpClient,
    val endpoint: String
) : MultiCallCaller by multicallCaller {
    private val logger = LoggerFactory.getLogger(this::class.java)

    val mapper = jacksonObjectMapper().configure(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
    )

    suspend fun getNativeBalance(address: String): BigDecimal = withContext(Dispatchers.IO) {
        val balance: BigInteger = httpClient.get("$endpoint/balance/$address").body()
        balance.toBigDecimal().dividePrecisely(
            BigDecimal.TEN.pow(18)
        )
    }

    suspend fun readMultiCall(
        elements: List<ContractCall>,
    ): List<MultiCallResult> {
        return multicallCaller.readMultiCall(elements) { address, function ->
            executeCall(address, function)
        }
    }
    suspend fun getEventsAsEthLog(getEventsLog: GetEventLogsCommand): List<LogObject> {
        val result = httpClient.post("$endpoint/events/logs") {
            contentType(ContentType.Application.Json)
            setBody(getEventsLog)
        }
        return if (result.status.isSuccess()) {
            val body: String = result.body()
            val parsed = JsonParser.parseString(body).asJsonObject
            if (parsed.has("error") && !parsed["error"].isJsonNull) {
                logger.error("Unable to get events from blockchain, result was ${result.bodyAsText()}")
                return kotlin.collections.emptyList()
            }
            parsed["result"].asJsonArray.map {
                mapper.readValue(it.toString(), LogObject::class.java)
            }
        } else {
            logger.error("Unable to get events from blockchain, result was ${result.bodyAsText()}")
            return emptyList()
        }
    }

    suspend fun getEventsAsEthLog(commands: List<GetEventLogsCommand>): List<EthLog> {
        val result = httpClient.post("$endpoint/events/logs?bulk") {
            contentType(ContentType.Application.Json)
            setBody(commands)
        }
        return if (result.status.isSuccess()) {
            result.body()
        } else {
            emptyList()
        }
    }

    suspend fun getLogs(txId: String): List<Log> = withContext(Dispatchers.IO) {
        httpClient.get("$endpoint/tx/${txId}/logs").body()
    }

    suspend fun getTransaction(txId: String): TransactionVO? = withContext(Dispatchers.IO) {
        val response = httpClient.get("$endpoint/tx/${txId}")
        if (response.status.isSuccess()) {
            response.body()
        } else {
            null
        }
    }

    class TransactionVO(
        val hash: String,
        val blockNumber: BigInteger,
        val from: String,
        val to: String?,
        val time: Long,
        val value: BigInteger,
        val possibleSpam: Boolean,
    )


    suspend fun readFunction(
        address: String,
        function: String,
        inputs: List<Type<*>>,
        outputs: List<TypeReference<out Type<*>>>? = null
    ): List<Type<*>> {
        logger.debug("reading $function from $address")
        return executeCall(address, createFunction(function, inputs, outputs))
    }

    suspend fun executeCall(
        address: String,
        function: org.web3j.abi.datatypes.Function,
    ): List<Type<*>> {
        val encodedFunction = FunctionEncoder.encode(function)
        val ethCall = call(null, address, encodedFunction) ?: return emptyList()
        return FunctionReturnDecoder.decode(ethCall.value, function.outputParameters)
    }

    suspend fun call(
        from: String?,
        contract: String,
        encodedFunction: String
    ): EthCall? = withContext(Dispatchers.IO) {
        retry(limitAttempts(5) + binaryExponentialBackoff(200, 3000)) {
            val post = httpClient.post("$endpoint/contract/call") {
                contentType(ContentType.Application.Json)
                setBody(
                    EvmContractInteractionCommand(
                        from = from,
                        contract = contract,
                        function = encodedFunction
                    )
                )
            }

            if (!post.status.isSuccess()) {
                null
            } else {
                post.body()
            }
        }
    }


    companion object {
        fun createFunction(
            method: String,
            inputs: List<Type<*>> = kotlin.collections.emptyList(),
            outputs: List<TypeReference<out Type<*>>>? = kotlin.collections.emptyList()
        ): org.web3j.abi.datatypes.Function {
            return Web3Function(
                method,
                inputs,
                outputs
            )
        }

        val MAX_UINT256 = BigInteger.TWO.pow(256).minus(BigInteger.ONE).toUint256()
    }
}
