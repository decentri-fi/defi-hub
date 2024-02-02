package io.defitrack.protocol.bancor.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

class PoolTokenContract(
    gateway: BlockchainGateway,
    address: String
) : ERC20Contract(
    gateway, address
) {

    val reserveToken = constant<String>("reserveToken", TypeUtils.address())
}