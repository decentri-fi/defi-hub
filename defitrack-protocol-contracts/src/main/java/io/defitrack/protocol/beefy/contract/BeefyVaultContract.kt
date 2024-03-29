package io.defitrack.protocol.beefy.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.protocol.beefy.domain.BeefyVault
import kotlinx.coroutines.Deferred
import java.math.BigInteger

context(BlockchainGateway)
class BeefyVaultContract(
    address: String,
    val beefyVault: BeefyVault
) : ERC20Contract(address) {

    fun fullExitFunction(): ContractCall {
        return createFunction(
            method = "withdrawAll",
        )
    }

    val balance: Deferred<BigInteger> = constant("balance", uint256())

    fun depositFunction(amount: BigInteger): ContractCall {
        return createFunction("deposit", listOf(amount.toUint256()))
    }

    val pricePerFullShare = constant<BigInteger>("getPricePerFullShare", uint256())

    suspend fun want(): String {
        var read = read(
            "want",
            inputs = emptyList(),
            outputs = listOf(address())
        )
        if (read.isEmpty()) {
            read = read(
                "wmatic",
                inputs = emptyList(),
                outputs = listOf(
                    address()
                )
            )
        }
        if (read.isEmpty()) {
            read = read(
                "token",
                inputs = emptyList(),
                outputs = listOf(address())
            )
        }
        if (read.isEmpty()) {
            read = read(
                "wbnb",
                inputs = emptyList(),
                outputs = listOf(address())
            )
        }
        return read[0].value as String
    }
}