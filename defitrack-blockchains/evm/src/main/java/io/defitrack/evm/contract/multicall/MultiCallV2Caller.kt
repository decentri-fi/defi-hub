package io.defitrack.evm.contract.multicall

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toBool
import org.apache.commons.codec.binary.Hex
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type

class MultiCallV2Caller(val address: String) : MultiCallCaller {

    override suspend fun readMultiCall(
        elements: List<MultiCallElement>,
        executeCall: suspend (address: String, function: Function) -> List<Type<*>>
    ): List<MultiCallResult> {
        if (elements.isEmpty()) {
            return emptyList()
        } else if (elements.size > 500) {
            return elements.chunked(100).map {
                readMultiCall(it, executeCall)
            }.flatten()
        }

        val encodedFunctions = elements.map {
            DynamicStruct(
                it.address.toAddress(),
                DynamicBytes(Hex.decodeHex(FunctionEncoder.encode(it.function).substring(2)))
            )
        }

        val aggregateFunction = Function(
            "tryAggregate",
            listOf(
                false.toBool(),
                DynamicArray(
                    encodedFunctions
                )
            ),
            listOf(
                object : TypeReference<DynamicArray<AggregateResult>>() {})
        )

        val executedCall = executeCall(address, aggregateFunction)
        return if (executedCall.isEmpty()) {
            println("empty multicall, it failed")
            elements.map {
                MultiCallResult(false, emptyList())
            }
        } else {
            val results = executedCall[0].value as List<AggregateResult>
            results.mapIndexed { index, result ->
                val element = elements[index]
                MultiCallResult(
                    result.success,
                    FunctionReturnDecoder.decode(Hex.encodeHexString(result.data), element.function.outputParameters)
                )
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