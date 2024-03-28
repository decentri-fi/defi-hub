package io.defitrack.mapper

import io.defitrack.network.toVO
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransactionVO
import org.web3j.abi.FunctionEncoder

fun PreparedTransaction.toVO(): PreparedTransactionVO {
    return PreparedTransactionVO(
        network = network.toVO(),
        data = FunctionEncoder.encode(function),
        to = to,
        from = from
    )
}