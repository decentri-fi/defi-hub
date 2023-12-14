package io.defitrack.protocol.prisma

import arrow.core.nel
import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class TroveManagerContract(blockchainGateway: BlockchainGateway, address: String) : EvmContract(
    blockchainGateway, address
) {

    val totalCollateralSnapshot = constant<BigInteger>("totalCollateralSnapshot", uint256())
    val debtToken = constant<String>("debtToken", TypeUtils.address())
    val collateralToken = constant<String>("collateralToken", TypeUtils.address())

    fun getTroveCollAndDebt(user: String): ContractCall {
        return createFunction(
            "getTroveCollAndDebt",
            user.toAddress().nel(),
            listOf(
                uint256(),
                uint256()
            )
        )
    }

    fun claimableReward(user: String): ContractCall {
        return createFunction("claimableReward", listOf(user.toAddress()), listOf(uint256()))
    }

    fun claimFn(user: String): ContractCall {
        return createFunction(
            "claimReward",
            listOf(user.toAddress()),
            emptyList()
        )
    }
}