package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.bytes32
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.apache.commons.codec.binary.Hex
import java.math.BigInteger

context(BlockchainGateway)
class BalancerPoolContract(address: String) : ERC20Contract(address) {

    val poolId = constant<ByteArray>("getPoolId", bytes32())
    val vault = constant<String>("getVault", address())
    val actualSupply = constant<BigInteger>("getActualSupply", uint256())

    suspend fun getPoolId(): String = Hex.encodeHexString(poolId.await())
}