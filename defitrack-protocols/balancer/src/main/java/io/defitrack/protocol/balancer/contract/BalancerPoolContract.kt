package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

class BalancerPoolContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC20Contract(
    blockchainGateway, "", address
) {

    suspend fun getPoolId(): String {
        return readSingle("getPoolId", TypeUtils.bytes32())
    }

    suspend fun getVault(): String {
        return readSingle("getVault", TypeUtils.address())
    }
}