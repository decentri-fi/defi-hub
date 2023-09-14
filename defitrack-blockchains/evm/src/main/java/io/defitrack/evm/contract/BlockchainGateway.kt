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
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.abi.domain.AbiContractFunction
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.evm.contract.multicall.MultiCallCaller
import io.defitrack.evm.contract.multicall.MultiCallResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.core.methods.response.EthCall
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
        elements: List<MultiCallElement>,
    ): List<MultiCallResult> {
        return multicallCaller.readMultiCall(elements) { address, function ->
            executeCall(address, function)
        }
    }

    suspend fun getEvents(getEventsLog: GetEventLogsCommand): String? {
        val result = httpClient.post("$endpoint/events/logs") {
            contentType(ContentType.Application.Json)
            setBody(getEventsLog)
        }
        return if (result.status.isSuccess()) {
            result.body()
        } else {
            null
        }
    }

    suspend fun getEventsAsEthLog(getEventsLog: GetEventLogsCommand): List<LogObject> {
        val result = httpClient.post("$endpoint/events/logs") {
            contentType(ContentType.Application.Json)
            setBody(getEventsLog)
        }
        return if (result.status.isSuccess()) {
            val body: String = result.body()
            JsonParser.parseString(body).asJsonObject["result"].asJsonArray.map {
                mapper.readValue(it.toString(), LogObject::class.java)
            }
        } else {
            logger.error("Unable to get events from blockchain, result was ${result.bodyAsText()}")
            return emptyList()
        }
    }

    suspend fun getLogs(txId: String): List<Log> = withContext(Dispatchers.IO) {
        httpClient.get("$endpoint/tx/${txId}/logs").body()
    }


    suspend fun readFunction(
        address: String,
        function: String,
        inputs: List<Type<*>>,
        outputs: List<TypeReference<out Type<*>>>? = null
    ): List<Type<*>> {
        return executeCall(address, createFunction(function, inputs, outputs))
    }

    open suspend fun executeCall(
        address: String,
        function: org.web3j.abi.datatypes.Function,
    ): List<Type<*>> {
        val encodedFunction = FunctionEncoder.encode(function)
        val ethCall = call(null, address, encodedFunction)
        return FunctionReturnDecoder.decode(ethCall.value, function.outputParameters)
    }

    suspend fun call(
        from: String?,
        contract: String,
        encodedFunction: String
    ): EthCall = withContext(Dispatchers.IO) {
        retry(limitAttempts(5) + binaryExponentialBackoff(1000, 10000)) {
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
            post.body()
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

        fun createFunctionWithAbi(
            function: AbiContractFunction,
            inputs: List<Type<*>>?,
            outputs: List<TypeReference<out Type<*>>>? = null
        ): org.web3j.abi.datatypes.Function {
            return Web3Function(function.name,
                inputs ?: emptyList(),
                outputs ?: function.outputs
                    .map { it.type }
                    .map { fromDataTypes(it, false) }
                    .toList()
            )
        }

        fun fromDataTypes(type: String, indexed: Boolean): TypeReference<out Type<*>>? {
            return try {
                when (type) {
                    "dynamicbytes" -> {
                        val typeclass = Class.forName("org.web3j.abi.datatypes.DynamicBytes") as Class<Type<*>>
                        indexedTypeReference(typeclass, indexed)
                    }

                    "tuple[]" -> {
                        null
                    }

                    "tuple" -> {
                        val typeclass = Class.forName("org.web3j.abi.datatypes.DynamicStruct") as Class<Type<*>>
                        indexedTypeReference(typeclass, indexed)
                    }

                    else -> {
                        val typeClass =
                            Class.forName("org.web3j.abi.datatypes." + StringUtils.capitalize(type)) as Class<Type<*>>
                        indexedTypeReference(typeClass, indexed)
                    }
                }
            } catch (ex: Exception) {
                fromGeneratedDataTypes(type, indexed)
            }
        }

        private fun fromGeneratedDataTypes(type: String, indexed: Boolean): TypeReference<out Type<*>> {
            return try {
                val typeClass =
                    Class.forName("org.web3j.abi.datatypes.generated." + StringUtils.capitalize(type)) as Class<Type<*>>
                indexedTypeReference(typeClass, indexed)
            } catch (ex: Exception) {
                val typeClass =
                    Class.forName("org.web3j.abi.datatypes.Utf8String") as Class<Type<*>>
                indexedTypeReference(typeClass, indexed)
            }
        }

        fun <T : Type<*>> indexedTypeReference(cls: Class<T>, indexed: Boolean): TypeReference<T> {
            return object : TypeReference<T>() {
                override fun getType(): java.lang.reflect.Type {
                    return cls
                }

                override fun isIndexed(): Boolean {
                    return indexed
                }
            }
        }


        val MAX_UINT256 = BigInteger.TWO.pow(256).minus(BigInteger.ONE).toUint256()
    }

    class GetEventLogsCommand(
        val addresses: List<String>,
        val topic: String,
        val optionalTopics: List<String?> = kotlin.collections.emptyList(),
        val fromBlock: BigInteger? = null,
        val toBlock: BigInteger? = null
    )
}
