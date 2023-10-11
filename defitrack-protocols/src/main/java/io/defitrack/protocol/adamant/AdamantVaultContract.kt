package io.defitrack.protocol.adamant

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function

class AdamantVaultContract(
    solidityBasedContractAccessor: BlockchainGateway,
    address: String,
) : ERC20Contract(solidityBasedContractAccessor, address) {

    suspend fun token(): String {
        return readSingle("token", address())
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

    fun getClaimFunction(): ContractCall {
        return createFunction(
            "claim",
            emptyList(),
            emptyList()
        ).toContractCall()
    }
}