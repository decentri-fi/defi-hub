package io.defitrack.protocol.adamant

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class AdamantVaultContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : ERC20Contract(solidityBasedContractAccessor, abi, address) {

    suspend fun token(): String {
        return readWithAbi("token")[0].value as String
    }

    fun getPendingRewardFunction(address: String): Function {
        return createFunction(
            "getPendingReward",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                uint256()
            )
        )
    }

    fun getClaimFunction(): Function {
        return createFunctionWithAbi(
            "claim",
            emptyList(),
            emptyList()
        )
    }
}