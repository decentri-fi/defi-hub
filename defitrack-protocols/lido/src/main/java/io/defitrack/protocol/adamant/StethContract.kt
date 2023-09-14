package io.defitrack.protocol.adamant

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class StethContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    suspend fun getTotalShares(): BigInteger {
        return read(
            "getTotalShares",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun getPooledEthByShares(shares: BigInteger): BigInteger {
        return read(
            "getPooledEthByShares",
            inputs = listOf(shares.toUint256()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun getTotalSupply(): BigInteger {
        return read(
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