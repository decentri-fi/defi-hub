package io.defitrack.protocol.bancor.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

context(BlockchainGateway)
class PoolTokenContract(address: String) : ERC20Contract(address) {
    val reserveToken = constant<String>("reserveToken", TypeUtils.address())
}