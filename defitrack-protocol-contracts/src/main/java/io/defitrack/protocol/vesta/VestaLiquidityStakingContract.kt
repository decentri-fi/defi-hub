package io.defitrack.protocol.vesta

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.abi.TypeUtils.Companion.uint64
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class VestaLiquidityStakingContract(
    address: String
) : EvmContract(address) {

    fun balances(user: String): ContractCall {
        return createFunction("balances", user.toAddress().nel(), uint256().nel())
    }

    fun earned(user: String): ContractCall {
        return createFunction("earned", user.toAddress().nel(), uint256().nel())
    }

    val vsta = constant<String>("vsta", address())
    val stakingToken = constant<String>("stakingToken", address())
    val lastTimeRewardApplicable = constant<BigInteger>("lastTimeRewardApplicable", uint64())

    fun getReward(): ContractCall {
        return createFunction("getReward")
    }
}