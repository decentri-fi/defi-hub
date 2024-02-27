package io.defitrack.protocol.lido

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

context(BlockchainGateway)
class WSTEthContract(
    address: String
) : ERC20Contract(
    address
) {

    suspend fun getStethByWstethFunction(wsteth: BigInteger): BigInteger {
        return readSingle(
            "getStETHByWstETH",
            inputs = listOf(wsteth.toUint256()),
            uint256()
        )
    }

    fun sharesOfFunction(address: String): ContractCall {
        return createFunction(
            "sharesOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(uint256())
        )
    }
}