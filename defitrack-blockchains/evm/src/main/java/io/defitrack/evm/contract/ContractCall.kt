package io.defitrack.evm.contract

import io.defitrack.common.network.Network
import org.web3j.abi.datatypes.Function

data class ContractCall(
    val function: Function,
    val network: Network,
    val address: String
)