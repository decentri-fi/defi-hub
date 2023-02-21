package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC721Contract
import io.defitrack.evm.contract.multicall.MultiCallElement
import java.math.BigInteger

class QidaoVaultContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC721Contract(
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

    suspend fun getVaults(user: String): List<Int> {
        return blockchainGateway.readMultiCall(
            (0 until vaultCount().toInt()).map {
                ownerOfFunction(it.toBigInteger())
            }.map {
                MultiCallElement(
                    it, address
                )
            }
        ).mapIndexed { index, value ->
            if ((value[0].value as String).lowercase() == user.lowercase()) {
                return@mapIndexed index
            } else {
                return@mapIndexed null
            }
        }.filterNotNull()
    }
}