package io.defitrack.evm.contract

import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type

abstract class EvmContract(
    val evmContractAccessor: EvmContractAccessor,
    val abi: String,
    val address: String
) {

    fun createFunction(
        method: String,
        inputs: List<Type<*>> = emptyList(),
        outputs: List<TypeReference<out Type<*>>?>? = null
    ): Function {
        return EvmContractAccessor.createFunction(
            evmContractAccessor.getFunction(abi, method),
            inputs,
            outputs
        )
    }


    fun readConstant(method: String): List<Type<*>> {
        return evmContractAccessor.readFunction(
            address = address,
            function = evmContractAccessor.getConstantFunction(
                abi,
                method
            )
        )
    }

    fun readMultiple(requests: List<ReadRequest>): List<List<Type<*>>> {
        val functions = requests.map {
            val abiFunction = evmContractAccessor.getFunction(abi, it.method)
            val function = EvmContractAccessor.createFunction(
                abiFunction, it.inputs, it.outputs
            )
            MultiCallElement(
                function,
                address
            )
        }
        return evmContractAccessor.readMultiCall(functions)
    }

    fun read(
        method: String,
        inputs: List<Type<*>> = emptyList(),
        outputs: List<TypeReference<out Type<*>>?>? = null
    ): List<Type<*>> {
        return evmContractAccessor.readFunction(
            address = address,
            inputs = inputs,
            outputs = outputs,
            function = evmContractAccessor.getFunction(
                abi,
                method
            )
        )
    }

    fun readConstant(method: String, inputs: List<Type<*>>): List<Type<*>> {
        return evmContractAccessor.readFunction(
            address = address,
            inputs = inputs,
            function = evmContractAccessor.getConstantFunction(
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
        return evmContractAccessor.readFunction(
            address = address,
            inputs = inputs,
            function = evmContractAccessor.getConstantFunction(
                abi,
                method
            ),
            outputs = outputs
        )
    }
}
