package io.defitrack.protocol.aave.staking

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import org.web3j.abi.datatypes.Function
import io.defitrack.evm.contract.BlockchainGateway.Companion.MAX_UINT256
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract

class StakedAaveContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC20Contract(
    blockchainGateway,
    address
) {

    fun getClaimRewardsFunction(user: String): ContractCall {
        return createFunction(
            "claimRewards",
            inputs = listOf(
                user.toAddress(),
                MAX_UINT256
            )
        )
    }

    fun getTotalRewardFunction(user: String): ContractCall {
        return createFunction(
            method = "getTotalRewardsBalance",
            inputs = listOf(user.toAddress()),
            outputs = listOf(
                uint256()
            )
        )
    }
}