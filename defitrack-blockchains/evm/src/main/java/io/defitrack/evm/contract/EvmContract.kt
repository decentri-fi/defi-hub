package io.defitrack.evm.contract

import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.evm.contract.multicall.MultiCallResult
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type

abstract class EvmContract(
    val blockchainGateway: BlockchainGateway,
    val abi: String,
    val address: String
) {

    companion object {
        fun createFunction(
            method: String,
            inputs: List<Type<*>> = emptyList(),
            outputs: List<TypeReference<out Type<*>>>? = emptyList()
        ): Function {
            return BlockchainGateway.createFunction(method, inputs, outputs)
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
}

