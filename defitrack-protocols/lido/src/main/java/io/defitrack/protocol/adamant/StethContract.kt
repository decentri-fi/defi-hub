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
    blockchainGateway, address
) {

    suspend fun getTotalShares(): BigInteger {
        return readSingle("getTotalShares", uint256())
    }

    suspend fun getPooledEthByShares(shares: BigInteger): BigInteger {
        return readSingle(
            "getPooledEthByShares",
            inputs = listOf(shares.toUint256()),
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