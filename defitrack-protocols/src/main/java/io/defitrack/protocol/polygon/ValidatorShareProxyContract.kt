package io.defitrack.protocol.polygon

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class ValidatorShareProxyContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {

    fun withdrawRewards(): ContractCall {
        return createFunction(
            "withdrawRewards",
        )
    }

    fun getLiquidRewards(user: String): ContractCall {
        return createFunction(
            "getLiquidRewards",
            inputs = listOf(user.toAddress()),
            outputs = listOf(uint256())
        )
    }

    fun getTotalStake(user: String): ContractCall {
        return createFunction(
            "getTotalStake",
            inputs = listOf(user.toAddress()),
            outputs = listOf(uint256(), uint256())
        )
    }
}