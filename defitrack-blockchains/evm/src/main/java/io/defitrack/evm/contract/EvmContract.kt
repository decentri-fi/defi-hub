package io.defitrack.evm.contract

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils
import io.defitrack.evm.multicall.MultiCallElement
import io.defitrack.evm.multicall.MultiCallResult
import kotlinx.coroutines.Deferred
import org.slf4j.LoggerFactory
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type

abstract class EvmContract(
    val blockchainGateway: BlockchainGateway,
    val address: String
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        fun createFunction(
            method: String,
            inputs: List<Type<*>> = emptyList(),
            outputs: List<TypeReference<out Type<*>>>? = emptyList()
        ): Function {
            return BlockchainGateway.createFunction(method, inputs, outputs)
        }

        fun createFunction(
            method: String,
            output: TypeReference<out Type<*>>
        ): Function {
            return BlockchainGateway.createFunction(method, emptyList(), listOf(output))
        }
    }

    val resolvedConstants: Deferred<Map<Function, MultiCallResult>> = AsyncUtils.lazyAsync {
        logger.debug("reading ${constantFunctions.size} constants from $address")
        readMultiCall(constantFunctions).mapIndexed { index, result ->
            constantFunctions[index] to result
        }.toMap()
    }

    val constantFunctions = mutableListOf<Function>()

    fun addConstant(function: Function): Function {
        constantFunctions.add(function)
        return function
    }

    inline fun <reified T : Any> constant(
        method: String,
        output: TypeReference<out Type<*>>
    ): Deferred<T> {
        val function = addConstant(createFunction(method, output))
        return constant(function)
    }


    inline fun <reified T : Any> constant(
        method: String,
        inputs: List<Type<*>> = emptyList(),
        outputs: List<TypeReference<out Type<*>>>? = emptyList()
    ): Deferred<T> {
        val function = addConstant(createFunction(method, inputs, outputs))
        return constant(function)
    }

    inline fun <reified T : Any> constant(
        function: Function,
    ): Deferred<T> {
        return AsyncUtils.lazyAsync {
            val get = resolvedConstants.await().get(function)!!
            if (get.success) {
                get.data[0].value as T
            } else {
                throw RuntimeException("Unable to read constant ${function.name} on $address")
            }
        }
    }

    suspend fun readMultiCall(
        functions: List<Function>
    ): List<MultiCallResult> {
        return blockchainGateway.readMultiCall(
            functions.map {
                MultiCallElement(
                    it,
                    address
                )
            }
        )
    }

    suspend fun read(
        method: String,
        inputs: List<Type<*>> = emptyList(),
        outputs: List<TypeReference<out Type<*>>>? = null
    ): List<Type<*>> {
        return blockchainGateway.readFunction(
            address = address,
            inputs = inputs,
            outputs = outputs,
            function = method
        )
    }

    suspend inline fun <reified T : Any> readSingle(
        method: String,
        inputs: List<Type<*>>,
        output: TypeReference<out Type<*>>
    ): T {
        return blockchainGateway.readFunction(
            address = address,
            inputs = inputs,
            outputs = listOf(output),
            function = method
        )[0].value as T
    }

    suspend inline fun <reified T : Any> readSingle(function: String, output: TypeReference<out Type<*>>): T {
        return read(
            function,
            outputs = listOf(output)
        )[0].value as T
    }

    fun Function.toContractCall(): ContractCall {
        return ContractCall(
            this,
            blockchainGateway.network,
            this@EvmContract.address
        )
    }

    data class ContractCall(
        val function: Function,
        val network: Network,
        val address: String
    )
}

