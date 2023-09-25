package io.defitrack.transaction

import io.defitrack.network.NetworkVO
import net.minidev.json.annotate.JsonIgnore
import org.web3j.abi.FunctionEncoder

class PreparedTransactionVO(
    val network: NetworkVO,
    val data: String,
    val to: String,
    val from: String? = null
)