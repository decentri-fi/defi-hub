package io.defitrack.protocol.bancor.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

class PoolTokenContract(
    gateway: BlockchainGateway,
    abi: String,
    address: String
) : ERC20Contract(
    gateway, abi, address
) {

    val reserveToken: String by lazy {
        readWithAbi("reserveToken")[0].value as String
    }
}