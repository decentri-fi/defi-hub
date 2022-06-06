package io.defitrack.evm.contract

import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.abi.domain.AbiContractFunction
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.StringUtils
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.Utils
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.generated.*
import org.web3j.protocol.core.methods.response.EthCall
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Collections.emptyList
import org.web3j.abi.datatypes.Function as Web3Function


open class BlockchainGateway(
    val abiDecoder: AbiDecoder,
    val network: Network,
    val multicallContractAddress: String,
    val httpClient: HttpClient,
    val endpoint: String
) {

    fun getNativeBalance(address: String): BigDecimal = runBlocking {
        val balance: BigInteger = httpClient.get("$endpoint/balance/$address").body()
        balance.toBigDecimal().dividePrecisely(
            BigDecimal.TEN.pow(18)
        )
    }

    open fun readMultiCall(elements: List<MultiCallElement>): List<List<Type<*>>> {
        val encodedFunctions = elements.map {
            DynamicStruct(
                it.address.toAddress(),
                DynamicBytes(Hex.decodeHex(FunctionEncoder.encode(it.function).substring(2)))
            )
        }

        val aggregateFunction = org.web3j.abi.datatypes.Function(
            "aggregate",
            listOf(
                DynamicArray(
                    encodedFunctions
                )
            ),
            listOf(object : TypeReference<Uint256?>() {},
                object : TypeReference<DynamicArray<DynamicBytes?>?>() {})
        )

        val data = executeCall(multicallContractAddress, aggregateFunction)[1].value as List<DynamicBytes>

        return data.map {
            val element = elements[data.indexOf(it)]
            FunctionReturnDecoder.decode(Hex.encodeHexString(it.value), element.function.outputParameters)
        }
    }


    fun readFunction(
        address: String,
        method: String,
    ): List<Type<*>> {
        return executeCall(address, createFunction(method, kotlin.collections.emptyList(), null))
    }

    fun readFunction(
        address: String,
        function: String,
        inputs: List<Type<*>>,
        outputs: List<TypeReference<out Type<*>>>? = null
    ): List<Type<*>> {
        return executeCall(address, createFunction(function, inputs, outputs))
    }

    fun readFunctionWithAbi(
        address: String,
        function: AbiContractFunction,
    ): List<Type<*>> {
        return executeCall(address, createFunctionWithAbi(function, null, null))
    }

    fun readFunctionWithAbi(
        address: String,
        function: AbiContractFunction,
        inputs: List<Type<*>>,
        outputs: List<TypeReference<out Type<*>>>? = null
    ): List<Type<*>> {
        return executeCall(address, createFunctionWithAbi(function, inputs, outputs))
    }

    fun executeCall(
        address: String,
        function: org.web3j.abi.datatypes.Function,
    ): List<Type<*>> {
        val encodedFunction = FunctionEncoder.encode(function)
        val ethCall = call(null, address, encodedFunction)
        return FunctionReturnDecoder.decode(ethCall.value, function.outputParameters)
    }

    fun call(
        from: String?,
        contract: String,
        encodedFunction: String
    ): EthCall = runBlocking(Dispatchers.IO) {
        retry(limitAttempts(5) + binaryExponentialBackoff(1000, 10000)) {
            httpClient.post("$endpoint/contract/call") {
                contentType(ContentType.Application.Json)
                setBody(
                    ContractInteractionCommand(
                        from = from,
                        contract = contract,
                        function = encodedFunction
                    )
                )
            }.body()
        }
    }

    fun getConstantFunction(abi: String, method: String): AbiContractFunction {
        return abiDecoder.decode(abi)
            .elements
            .filterIsInstance<AbiContractFunction>()
            .filter { it.isConstant }
            .first { it.name.equals(method, ignoreCase = true) }
    }

    fun getFunction(abi: String, method: String): AbiContractFunction {
        return abiDecoder.decode(abi)
            .elements
            .filterIsInstance<AbiContractFunction>()
            .first { it.name.equals(method, ignoreCase = true) }
    }

    companion object {

        fun createFunction(
            method: String,
            inputs: List<Type<*>> = kotlin.collections.emptyList(),
            outputs: List<TypeReference<out Type<*>>>? = null
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

        fun String.decodeAsFunctionResponse(outputs: List<String>): List<Type<Any>>? {
            return FunctionReturnDecoder.decode(
                this,
                Utils.convert(outputs.map {
                    it.toTypeReference()
                })
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

        fun BigInteger.toUint256(): Uint256 {
            return Uint256(this)
        }

        fun BigInteger.toUint8(): Uint8 {
            return Uint8(this)
        }

        fun BigInteger.toUint16(): Uint16 {
            return Uint16(this)
        }

        fun BigInteger.Int128(): Int128 {
            return Int128(this)
        }

        fun BigInteger.toInt128(): Int128 {
            return Int128(this)
        }

        fun String.toTypeReference(): TypeReference<out Type<*>>? {
            return fromDataTypes(this, false)
        }

        fun uint256(): TypeReference<Uint256> {
            return TypeReference.create(Uint256::class.java)
        }

        fun address(): TypeReference<Address> {
            return TypeReference.create(Address::class.java)
        }

        fun uint40(): TypeReference<Uint40> {
            return TypeReference.create(Uint40::class.java)
        }


        fun bool(): TypeReference<Bool> {
            return TypeReference.create(Bool::class.java)
        }

        fun String.toAddress(): Address {
            return Address(this)
        }

        fun Boolean.toBool(): Bool {
            return Bool(this)
        }

        val MAX_UINT256 = BigInteger.TWO.pow(64).minus(BigInteger.ONE).toUint256()
    }
}
