package io.defitrack.transaction

import io.defitrack.evm.contract.EvmContract
import io.defitrack.network.NetworkVO
import io.defitrack.network.toVO
import org.web3j.abi.FunctionEncoder

data class PreparedTransaction(
    val network: NetworkVO,
    val function: org.web3j.abi.datatypes.Function,
    val to: String,
    val from: String? = null
) {

    companion object {
        suspend fun selfExecutingTransaction(mutableFunctionProvider: suspend (String) -> EvmContract.MutableFunction): suspend (String) -> PreparedTransaction {
            return { from ->
                PreparedTransaction(
                    mutableFunctionProvider(from), from
                )
            }
        }

        suspend fun selfExecutingTransaction(mutableFunctionProvider: suspend () -> EvmContract.MutableFunction): suspend (String) -> PreparedTransaction {
            return { from ->
                PreparedTransaction(
                    mutableFunctionProvider(), from
                )
            }
        }
    }

    constructor(mutableFunction: EvmContract.MutableFunction, from: String? = null) :
            this(
                network = mutableFunction.network.toVO(),
                function = mutableFunction.function,
                to = mutableFunction.address,
                from = from
            )

    val data = FunctionEncoder.encode(function)
}