package io.defitrack.protocol.beefy.contract

import arrow.core.nel
import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class BeefyLaunchPoolContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {

    val stakedToken = constant<String>("stakedToken", TypeUtils.address())

    fun earned(user: String): Function {
        return createFunction(
            "earned",
            user.toAddress().nel(),
            uint256().nel()
        )
    }

    fun getRewardfn(): MutableFunction {
        return createFunction(
            "getReward"
        ).toMutableFunction()
    }
}