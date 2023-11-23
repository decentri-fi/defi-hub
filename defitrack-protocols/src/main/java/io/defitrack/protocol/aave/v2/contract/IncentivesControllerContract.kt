package io.defitrack.protocol.aave.v2.contract

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.MAX_UINT256
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Function

class IncentivesControllerContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, address
) {

    fun claimRewardsFn(user: String): ContractCall {
        return createFunction(
            "claimRewards",
            inputs = listOf(
                object : DynamicArray<Address>() {},
                MAX_UINT256,
                user.toAddress()
            )
        )
    }

    fun getUserUnclaimedRewardsFn(user: String): ContractCall {
        return createFunction(
            "getUserUnclaimedRewards",
            listOf(user.toAddress()), listOf(uint256())
        )
    }
}