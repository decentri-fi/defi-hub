package io.defitrack.protocol.polygon

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class ValidatorShareProxyContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {

    fun withdrawRewards(): MutableFunction {
        return createFunction(
            "withdrawRewards",
        ).toMutableFunction()
    }

    fun getLiquidRewards(user: String): Function {
        return createFunction(
            "getLiquidRewards",
            inputs = listOf(user.toAddress()),
            outputs = listOf(uint256())
        )
    }

    fun getTotalStake(user: String): Function {
        return createFunction(
            "getTotalStake",
            inputs = listOf(user.toAddress()),
            outputs = listOf(uint256(), uint256())
        )
    }
}