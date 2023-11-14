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
        suspend fun selfExecutingTransaction(contractCallProvider: suspend (String) -> EvmContract.ContractCall): suspend (String) -> PreparedTransaction {
            return { from ->
                PreparedTransaction(
                    contractCallProvider(from), from
                )
            }
        }

        suspend fun selfExecutingTransaction(contractCallProvider: suspend () -> EvmContract.ContractCall): suspend (String) -> PreparedTransaction {
            return { from ->
                PreparedTransaction(
                    contractCallProvider(), from
                )
            }
        }
    }

    constructor(contractCall: EvmContract.ContractCall, from: String?) :
            this(
                network = contractCall.network.toVO(),
                function = contractCall.function,
                to = contractCall.address,
                from = from
            )

    val data = FunctionEncoder.encode(function)
}