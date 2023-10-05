package io.defitrack.transaction

import io.defitrack.evm.contract.EvmContract
import io.defitrack.network.NetworkVO
import io.defitrack.network.toVO
import net.minidev.json.annotate.JsonIgnore
import org.web3j.abi.FunctionEncoder

data class PreparedTransaction(
    val network: NetworkVO,
    val function: org.web3j.abi.datatypes.Function,
    val to: String,
    val from: String? = null
) {

    constructor(contractCall: EvmContract.ContractCall, from: String?) :
            this(
                network = contractCall.network.toVO(),
                function = contractCall.function,
                to = contractCall.address,
                from = from
            )

    val data = FunctionEncoder.encode(function)
}