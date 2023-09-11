package io.defitrack.evm.contract.multicall

import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type

interface MultiCallCaller {

    suspend fun readMultiCall(
        elements: List<MultiCallElement>,
        executeCall: suspend (address: String, function: Function) -> List<Type<*>>
    ): List<List<Type<*>>>
}