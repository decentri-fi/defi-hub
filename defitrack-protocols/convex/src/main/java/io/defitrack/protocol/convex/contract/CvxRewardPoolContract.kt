package io.defitrack.protocol.convex.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address

class CvxRewardPoolContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
    val name: String
) : EvmContract(solidityBasedContractAccessor, abi, address) {

    suspend fun stakingToken(): String {
        return readWithAbi(
            "stakingToken",
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )[0].value as String
    }

    suspend fun rewardToken(): String {
        return readWithAbi(
            "stakingToken",
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )[0].value as String
    }
}