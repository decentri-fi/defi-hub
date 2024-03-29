package io.defitrack.transaction

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractCall
import org.web3j.abi.FunctionEncoder

data class PreparedTransaction(
    val network: Network,
    val function: org.web3j.abi.datatypes.Function,
    val to: String,
    val from: String? = null
) {

    companion object {
        suspend fun selfExecutingTransaction(contractCallProvider: suspend (String) -> ContractCall): suspend (String) -> PreparedTransaction {
            return { from ->
                PreparedTransaction(
                    contractCallProvider(from), from
                )
            }
        }

        suspend fun selfExecutingTransaction(contractCallProvider: suspend () -> ContractCall): suspend (String) -> PreparedTransaction {
            return { from ->
                PreparedTransaction(
                    contractCallProvider(), from
                )
            }
        }
    }

    constructor(contractCall: ContractCall, from: String? = null) :
            this(
                network = contractCall.network,
                function = contractCall.function,
                to = contractCall.address,
                from = from
            )

    val data = FunctionEncoder.encode(function)
}