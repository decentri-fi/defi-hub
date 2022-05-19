package io.defitrack.transaction

import org.web3j.abi.FunctionEncoder

class PreparedTransaction(val function: org.web3j.abi.datatypes.Function) {
    val data = FunctionEncoder.encode(function)
}