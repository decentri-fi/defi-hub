package io.defitrack.protocol.quickswap.contract

import io.defitrack.abi.TypeUtils.Companion.dynamicArray
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256

class FeeQlpTrackerContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(blockchainGateway, address) {

    suspend fun getAllRewardTokens(): List<String> {
        return (read(
            "getAllRewardTokens",
            emptyList(),
            listOf(
                dynamicArray<Address>(),
            )
        )[0].value as List<Address>).map {
            it.value
        }
    }

    fun claimableFn(user: String): ContractCall {
        return createFunction(
            "claimableAll",
            listOf(user.toAddress()),
            listOf(
                dynamicArray<Address>(),
                dynamicArray<Uint256>(),
            )
        )
    }
}