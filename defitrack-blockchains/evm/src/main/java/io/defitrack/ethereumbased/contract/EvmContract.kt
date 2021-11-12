package io.defitrack.ethereumbased.contract

import io.defitrack.ethereumbased.contract.multicall.MultiCallElement
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type

abstract class EvmContract(
    val ethereumContractAccessor: EvmContractAccessor,
    val abi: String,
    val address: String
) {

    fun createFunction(
        method: String,
        inputs: List<Type<*>> = emptyList(),
        outputs: List<TypeReference<out Type<*>>?>? = null
    ): Function {
        return ethereumContractAccessor.createFunction(
            ethereumContractAccessor.getFunction(abi, method)!!,
            inputs,
            outputs
        )
    }


    fun readConstant(method: String): List<Type<*>> {
        return ethereumContractAccessor.readFunction(
            address = address,
            function = ethereumContractAccessor.getConstantFunction(
                abi,
                method
            )
        )
    }

    fun readMultiple(requests: List<ReadRequest>): List<List<Type<*>>> {
        val functions = requests.map {
            val abiFunction = ethereumContractAccessor.getFunction(abi, it.method)
            val function = ethereumContractAccessor.createFunction(
                abiFunction!!, it.inputs, it.outputs
            )
            MultiCallElement(
                function,
                address
            )
        }
        return ethereumContractAccessor.readMultiCall(functions)
    }

    fun read(
        method: String,
        inputs: List<Type<*>> = emptyList(),
        outputs: List<TypeReference<out Type<*>>?>? = null
    ): List<Type<*>> {
        return ethereumContractAccessor.readFunction(
            address = address,
            inputs = inputs,
            outputs = outputs,
            function = ethereumContractAccessor.getFunction(
                abi,
                method
            )
        )
    }

    fun readConstant(method: String, inputs: List<Type<*>>): List<Type<*>> {
        return ethereumContractAccessor.readFunction(
            address = address,
            inputs = inputs,
            function = ethereumContractAccessor.getConstantFunction(
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
        return ethereumContractAccessor.readFunction(
            address = address,
            inputs = inputs,
            function = ethereumContractAccessor.getConstantFunction(
                abi,
                method
            ),
            outputs = outputs
        )
    }
}
