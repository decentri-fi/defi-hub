package io.defitrack.multicall

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toBool
import io.defitrack.domain.ConstructedEthCall
import io.defitrack.evm.EvmContractInteractionCommand
import io.defitrack.evm.contract.MultiCallV2Caller
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.Function
import org.web3j.protocol.core.methods.response.EthCall

@Component
class MulticallService(
    @Value("\${multicallAddress:0x}") private val multicallAddress: String,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        logger.info("Multicall address: $multicallAddress")
    }

    suspend fun call(inputs: List<EvmContractInteractionCommand>, call: suspend (EvmContractInteractionCommand) -> EthCall): List<EthCall> {
       return if (multicallAddress == "0x") {
            return inputs.map {
                call(it)
            }
        } else {
            multicall(inputs, call)
       }
    }

    private suspend fun multicall(calls: List<EvmContractInteractionCommand>, call: suspend (EvmContractInteractionCommand) -> EthCall): List<ConstructedEthCall> {
        val encodedFunctions = calls.map {
            DynamicStruct(
                it.contract.toAddress(),
                DynamicBytes(Hex.decodeHex(it.function.removePrefix("0x")))
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
            listOf(TypeUtils.dynamicArray<MultiCallV2Caller.AggregateResult>())
        )

        val executedCall = call(
            EvmContractInteractionCommand(
                contract = multicallAddress,
                function = FunctionEncoder.encode(aggregateFunction),
                from = null
            )
        )

        val returnValues = FunctionReturnDecoder.decode(executedCall.value, aggregateFunction.outputParameters)

        val results = returnValues[0].value as List<MultiCallV2Caller.AggregateResult>
        return results.map { result ->
            ConstructedEthCall(
                result.success,
                Hex.encodeHexString(result.data)
            )
        }
    }


}