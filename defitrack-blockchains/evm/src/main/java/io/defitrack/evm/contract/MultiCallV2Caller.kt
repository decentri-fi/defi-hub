package io.defitrack.evm.contract

import arrow.fx.coroutines.parMap
import io.defitrack.abi.TypeUtils.Companion.dynamicArray
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toBool
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.Function

class MultiCallV2Caller(val address: String) : MultiCallCaller {

    val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun readMultiCall(
        elements: List<ContractCall>,
        executeCall: suspend (address: String, function: Function) -> List<Type<*>>
    ): List<MultiCallResult> {
        if (elements.isEmpty()) {
            return emptyList()
        } else if (elements.size > 200) {
            return elements.chunked(200).parMap(concurrency = 8) {
                readMultiCall(it, executeCall)
            }.flatten()
        }

        val encodedFunctions = elements.map {
            DynamicStruct(
                it.address.toAddress(),
                DynamicBytes(Hex.decodeHex(FunctionEncoder.encode(it.function).substring(2)))
            )
        }

        logger.debug("calling multicall with ${encodedFunctions.size} functions")

        val aggregateFunction = Function(
            "tryAggregate",
            listOf(
                false.toBool(),
                DynamicArray(
                    encodedFunctions
                )
            ),
            listOf(dynamicArray<AggregateResult>())
        )

        val executedCall = executeCall(address, aggregateFunction)
        return if (executedCall.isEmpty()) {
            logger.info("empty multicall, it failed")
            throw MulticallFailedException()
        } else {
            val results = executedCall[0].value as List<AggregateResult>
            results.mapIndexed { index, result ->
                val element = elements[index]
                try {
                    MultiCallResult(
                        result.success,
                        FunctionReturnDecoder.decode(
                            Hex.encodeHexString(result.data),
                            element.function.outputParameters
                        )
                    )
                } catch (ex: Exception) {
                    logger.error("unexpected exception during multicall: {}", ex.message)
                    MultiCallResult(false, emptyList())
                }

            }
        }
    }

    data class AggregateResult(val success: Boolean, val data: ByteArray) : DynamicStruct(
        success.toBool(),
        DynamicBytes(data)
    ) {
        constructor(_success: Bool, callData: DynamicBytes) : this(
            _success.value,
            callData.value
        )
    }
}