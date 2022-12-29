package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class QidaoVaultContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC20Contract(
    blockchainGateway, "", address
) {

    suspend fun vaultCount(): BigInteger {
        return readWithoutAbi(
            method = "vaultCount",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun vaultDebt(vaultId: BigInteger): BigInteger {
        return readWithoutAbi(
            "vaultDebt",
            inputs = listOf(vaultId.toUint256()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun collateral(): String {
        return readWithoutAbi(
            "collateral",
            outputs = listOf(address())
        )[0].value as String
    }

    suspend fun ownerOfFunction(tokenId: BigInteger): Function {
        return createFunction(
            "ownerOf",
            inputs = listOf(tokenId.toUint256()),
            outputs = listOf(address())
        )
    }
}