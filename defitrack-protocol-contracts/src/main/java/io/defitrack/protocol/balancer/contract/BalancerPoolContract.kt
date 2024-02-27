package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import kotlinx.coroutines.Deferred
import org.apache.commons.codec.binary.Hex
import java.math.BigInteger

context(BlockchainGateway)
class BalancerPoolContract(address: String) : ERC20Contract(address) {

    val poolId: Deferred<ByteArray> = constant("getPoolId", TypeUtils.bytes32())
    val vault: Deferred<String> = constant("getVault", TypeUtils.address())
    val actualSupply: Deferred<BigInteger> = constant("getActualSupply", TypeUtils.uint256())

    suspend fun getPoolId(): String {
        return Hex.encodeHexString(poolId.await())
    }
}