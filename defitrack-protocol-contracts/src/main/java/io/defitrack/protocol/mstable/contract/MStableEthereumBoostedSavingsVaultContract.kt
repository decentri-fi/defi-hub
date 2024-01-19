package io.defitrack.protocol.mstable.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract

class MStableEthereumBoostedSavingsVaultContract(
    ethereumContractAccessor: BlockchainGateway,
    address: String,
) : ERC20Contract(ethereumContractAccessor, address) {


    fun rawBalanceOfFunction(address: String): ContractCall {
        return createFunction(
            "rawBalanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(uint256())
        )
    }

    suspend fun rewardsToken(): String {
        return readSingle("rewardsToken", TypeUtils.address())
    }

    suspend fun stakingToken(): String {
        return readSingle("stakingToken", TypeUtils.address())
    }
}