package io.defitrack.evm.contract

import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type

abstract class EvmContract(
    val blockchainGateway: BlockchainGateway,
    val abi: String,
    val address: String
) {

    fun createFunction(
        method: String,
        inputs: List<Type<*>> = emptyList(),
        outputs: List<TypeReference<out Type<*>>>? = null
    ): Function {
        return BlockchainGateway.createFunction(method, inputs, outputs)
    }

    fun createFunctionWithAbi(
        method: String,
        inputs: List<Type<*>> = emptyList(),
        outputs: List<TypeReference<out Type<*>>>? = null
    ): Function {
        return BlockchainGateway.createFunctionWithAbi(
            blockchainGateway.getFunction(abi, method),
            inputs,
            outputs
        )
    }


    fun readConstant(method: String): List<Type<*>> {
        return blockchainGateway.readFunctionWithAbi(
            address = address,
            function = blockchainGateway.getConstantFunction(
                abi,
                method
            )
        )
    }

    fun readMultiple(requests: List<ReadRequest>): List<List<Type<*>>> {
        val functions = requests.map {
            val abiFunction = blockchainGateway.getFunction(abi, it.method)
            val function = BlockchainGateway.createFunctionWithAbi(
                abiFunction, it.inputs, it.outputs
            )
            MultiCallElement(
                function,
                address
            )
        }
        return blockchainGateway.readMultiCall(functions)
    }

    fun read(
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

    fun readWithAbi(
        method: String,
        inputs: List<Type<*>> = emptyList(),
        outputs: List<TypeReference<out Type<*>>>? = null
    ): List<Type<*>> {
        return blockchainGateway.readFunctionWithAbi(
            address = address,
            inputs = inputs,
            outputs = outputs,
            function = blockchainGateway.getFunction(
                abi,
                method
            )
        )
    }

    fun readConstant(method: String, inputs: List<Type<*>>): List<Type<*>> {
        return blockchainGateway.readFunctionWithAbi(
            address = address,
            inputs = inputs,
            function = blockchainGateway.getConstantFunction(
                abi,
                method
            )
        )
    }

    fun readConstant(
        method: String,
        inputs: List<Type<*>>,
        outputs: List<TypeReference<out Type<*>>>? = null
    ): List<Type<*>> {
        return blockchainGateway.readFunctionWithAbi(
            address = address,
            inputs = inputs,
            function = blockchainGateway.getConstantFunction(
                abi,
                method
            ),
            outputs = outputs
        )
    }

    inline fun <reified T : Any> read(function: String): T {
        return readWithAbi(function)[0].value as T
    }
}
