package io.defitrack.protocol.adamant

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class WSTEthContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    suspend fun getStethByWstethFunction(wsteth: BigInteger): BigInteger {
        return readWithoutAbi(
            "getStETHByWstETH",
            inputs = listOf(wsteth.toUint256()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun stEthPerToken() {
        return readSingle("stEthPerToken", uint256())
    }

    suspend fun getTotalShares(): BigInteger {
        return readWithoutAbi(
            "getTotalShares",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun getTotalSupply(): BigInteger {
        return readWithoutAbi(
            "totalSupply",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    fun sharesOfFunction(address: String): Function {
        return createFunction(
            "sharesOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(uint256())
        )
    }

}