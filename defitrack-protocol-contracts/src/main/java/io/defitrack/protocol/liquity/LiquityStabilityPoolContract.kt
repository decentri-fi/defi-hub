package io.defitrack.protocol.liquity

import arrow.core.nonEmptyListOf
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class LiquityStabilityPoolContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(blockchainGateway, address) {

    fun deposits(user: String): ContractCall {
        return createFunction(
            "getCompoundedLUSDDeposit",
            nonEmptyListOf(user.toAddress()),
            nonEmptyListOf(uint256())
        )
    }

    fun lqtyGain(user: String): ContractCall {
        return createFunction(
            "getDepositorLQTYGain",
            nonEmptyListOf(user.toAddress()),
            nonEmptyListOf(uint256())
        )
    }

    fun ethGain(user: String): ContractCall {
        return createFunction(
            "getDepositorETHGain",
            nonEmptyListOf(user.toAddress()),
            nonEmptyListOf(uint256())
        )
    }

    fun claim(): ContractCall {
        return createFunction(
            "withdrawFromSP",
            nonEmptyListOf(BigInteger.ZERO.toUint256())
        )
    }
}