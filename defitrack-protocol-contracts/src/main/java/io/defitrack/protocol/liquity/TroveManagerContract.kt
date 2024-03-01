package io.defitrack.protocol.liquity

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class TroveManagerContract(address: String) : EvmContract(address) {

    val totalCollateralSnapshot = constant<BigInteger>("totalCollateralSnapshot", uint256())

    fun getTroveColl(user: String): ContractCall {
        return createFunction(
            "getTroveColl",
            user.toAddress().nel(),
            uint256().nel()
        )
    }

    fun getTroveDebt(user: String): ContractCall {
        return createFunction(
            "getTroveDebt",
            user.toAddress().nel(),
            uint256().nel()
        )
    }
}