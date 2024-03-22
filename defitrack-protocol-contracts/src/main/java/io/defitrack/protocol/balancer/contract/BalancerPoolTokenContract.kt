package io.defitrack.protocol.balancer.contract

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint24
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class BalancerPoolTokenContract(address: String) : EvmContract(address) {

    suspend fun getBalance(underlying: String): BigInteger {
        return readSingle(
            "getBalance",
            underlying.toAddress().nel(),
            uint256()
        )
    }
}