package io.defitrack.protocol.lido

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class WSTEthContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, address
) {

    suspend fun getStethByWstethFunction(wsteth: BigInteger): BigInteger {
        return readSingle(
            "getStETHByWstETH",
            inputs = listOf(wsteth.toUint256()),
            uint256()
        )
    }

    fun sharesOfFunction(address: String): Function {
        return createFunction(
            "sharesOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(uint256())
        )
    }
}