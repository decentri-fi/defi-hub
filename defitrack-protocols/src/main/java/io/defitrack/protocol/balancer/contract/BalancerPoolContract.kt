package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.apache.commons.codec.binary.Hex

class BalancerPoolContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC20Contract(
    blockchainGateway, address
) {

    suspend fun getPoolId(): String {
        val bytes: ByteArray = readSingle("getPoolId", TypeUtils.bytes32())
        return Hex.encodeHexString(bytes)
    }

    suspend fun getVault(): String {
        return readSingle("getVault", TypeUtils.address())
    }
}