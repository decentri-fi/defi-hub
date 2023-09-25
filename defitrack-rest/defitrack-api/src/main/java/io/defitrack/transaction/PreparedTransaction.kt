package io.defitrack.transaction

import io.defitrack.network.NetworkVO
import net.minidev.json.annotate.JsonIgnore
import org.web3j.abi.FunctionEncoder

class PreparedTransaction(
    val network: NetworkVO,
    val function: org.web3j.abi.datatypes.Function,
    val to: String,
    val from: String? = null
) {
    val data = FunctionEncoder.encode(function)
}