package io.defitrack.protocol.mstable.contract

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function

class MStableEthereumBoostedSavingsVaultContract(
    ethereumContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : ERC20Contract(ethereumContractAccessor, abi, address) {


    fun rawBalanceOfFunction(address: String): Function {
        return createFunctionWithAbi(
            "rawBalanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                uint256()
            )
        )
    }

    suspend fun rewardsToken(): String {
        return (readWithAbi(
            "rewardsToken"
        )[0].value as String)
    }

    suspend fun stakingToken(): String {
        return (readWithAbi(
            "stakingToken"
        )[0].value as String)
    }
}