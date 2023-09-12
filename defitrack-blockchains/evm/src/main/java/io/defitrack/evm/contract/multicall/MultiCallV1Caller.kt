package io.defitrack.evm.contract.multicall

import io.defitrack.abi.TypeUtils.Companion.toAddress
import org.apache.commons.codec.binary.Hex
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256

class MultiCallV1Caller(val address: String) : MultiCallCaller {

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
            "aggregate",
            listOf(
                DynamicArray(
                    encodedFunctions
                )
            ),
            listOf(object : TypeReference<Uint256?>() {},
                object : TypeReference<DynamicArray<DynamicBytes?>?>() {})
        )

        val executeCall = executeCall(address, aggregateFunction)
        if (executeCall.isEmpty()) {
            println("empty multicall, it failed")
            return elements.map {
                MultiCallResult(false, emptyList())
            }
        }
        val data = executeCall[1].value as List<DynamicBytes>
        return data.map {
            val element = elements[data.indexOf(it)]
            MultiCallResult(
                true,
                FunctionReturnDecoder.decode(Hex.encodeHexString(it.value), element.function.outputParameters)
            )
        }
    }

}