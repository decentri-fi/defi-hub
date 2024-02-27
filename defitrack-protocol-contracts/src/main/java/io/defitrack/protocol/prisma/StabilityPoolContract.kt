package io.defitrack.protocol.prisma

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class StabilityPoolContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(blockchainGateway, address) {

    fun claimableReward(user: String): ContractCall {
        return createFunction(
            "claimableReward",
            user.toAddress().nel(),
            uint256().nel()
        )
    }

    fun accountDeposits(user: String): ContractCall {
        return createFunction(
            "accountDeposits",
            user.toAddress().nel(),
            uint256().nel()
        )
    }

    fun claimFn(): ContractCall {
        return createFunction(
            "withdrawFromSP",
            listOf(BigInteger.ZERO.toUint256())
        )
    }
}