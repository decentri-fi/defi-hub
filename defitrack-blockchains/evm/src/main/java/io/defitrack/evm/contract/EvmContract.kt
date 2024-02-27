package io.defitrack.evm.contract

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.GetEventLogsCommand
import kotlinx.coroutines.Deferred
import org.slf4j.LoggerFactory
import org.web3j.abi.EventEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.core.methods.response.EthLog
import java.math.BigInteger

context(BlockchainGateway)
abstract class EvmContract(
    val address: String
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getLogs(event: Event, fromBlock: String, toBlock: String?): List<EthLog.LogObject> {
        return getEventsAsEthLog(
            GetEventLogsCommand(
                addresses = listOf(this.address),
                topic = EventEncoder.encode(event),
                fromBlock = BigInteger(fromBlock, 10),
                toBlock = toBlock?.let { BigInteger(toBlock, 10) }
            )
        )
    }

    fun createFunction(
        method: String,
        output: TypeReference<out Type<*>>
    ): ContractCall {
        return BlockchainGateway.createFunction(method, emptyList(), listOf(output)).toContractCall()
    }

    fun createFunction(
        method: String,
        inputs: List<Type<*>> = emptyList(),
        outputs: List<TypeReference<out Type<*>>>? = emptyList()
    ): ContractCall {
        return BlockchainGateway.createFunction(method, inputs, outputs).toContractCall()
    }

    var resolvedConstants: Deferred<Map<ContractCall, MultiCallResult>> = lazyAsync {
        logger.debug("reading ${constantFunctions.size} constants from $address")
        readMultiCall(constantFunctions).mapIndexed { index, result ->
            constantFunctions[index] to result
        }.toMap()
    }

    val constantFunctions = mutableListOf<ContractCall>()

    fun addConstant(function: ContractCall): ContractCall {
        constantFunctions.add(function)
        return function
    }

    inline fun <reified T : Any> constant(
        method: String,
        output: TypeReference<out Type<*>>,
        noinline extractor: ((List<Type<*>>) -> T)? = null
    ): Deferred<T> {
        val function = addConstant(createFunction(method, output))
        return createConstant(function, extractor)
    }

    inline fun <reified T : Any> constant(
        method: String,
        outputs: List<TypeReference<out Type<*>>>,
        noinline extractor: ((List<Type<*>>) -> T)? = null
    ): Deferred<T> {
        val function = addConstant(createFunction(method, emptyList(), outputs))
        return createConstant(function, extractor)
    }

    inline fun <reified T : Any> createConstant(
        function: ContractCall,
        noinline extractor: ((List<Type<*>>) -> T)?
    ): Deferred<T> {
        return lazyAsync {
            val get = resolvedConstants.await().get(function)!!
            if (get.success) {
                extractor?.invoke(get.data) ?: get.data[0].value as T
            } else {
                throw RuntimeException("Unable to read constant ${function.function.name} on $address")
            }
        }
    }

    suspend fun readMultiCall(functions: List<ContractCall>): List<MultiCallResult> {
        return readMultiCall(functions)
    }

    suspend fun read(
        method: String,
        inputs: List<Type<*>> = emptyList(),
        outputs: List<TypeReference<out Type<*>>>? = null
    ): List<Type<*>> {
        return readFunction(
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
        return readFunction(
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
            network,
            this@EvmContract.address
        )
    }


    fun resolveConstants(resolved: Map<ContractCall, MultiCallResult>) {
        this.resolvedConstants = lazyAsync {
            resolved
        }
    }

    fun getNetwork(): Network {
        return this@BlockchainGateway.network
    }
}

