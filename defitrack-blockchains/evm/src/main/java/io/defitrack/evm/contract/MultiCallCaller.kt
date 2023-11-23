package io.defitrack.evm.contract

import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type

interface MultiCallCaller {

    suspend fun readMultiCall(
        elements: List<ContractCall>,
        executeCall: suspend (address: String, function: Function) -> List<Type<*>>
    ): List<MultiCallResult>
}