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
        outputs: List<TypeReference<out Type<*>>?>? = null
    ): Function {
        return BlockchainGateway.createFunction(
            blockchainGateway.getFunction(abi, method),
            inputs,
            outputs
        )
    }


    fun readConstant(method: String): List<Type<*>> {
        return blockchainGateway.readFunction(
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
            val function = BlockchainGateway.createFunction(
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
        outputs: List<TypeReference<out Type<*>>?>? = null
    ): List<Type<*>> {
        return blockchainGateway.readFunction(
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
        return blockchainGateway.readFunction(
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
        outputs: List<TypeReference<out Type<*>>?>? = null
    ): List<Type<*>> {
        return blockchainGateway.readFunction(
            address = address,
            inputs = inputs,
            function = blockchainGateway.getConstantFunction(
                abi,
                method
            ),
            outputs = outputs
        )
    }
}
