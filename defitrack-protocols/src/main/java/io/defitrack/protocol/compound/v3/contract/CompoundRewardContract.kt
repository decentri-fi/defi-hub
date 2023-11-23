package io.defitrack.protocol.compound.v3.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.bool
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toBool
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.abi.TypeUtils.Companion.uint64
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

open class CompoundRewardContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, address
) {

    fun getRewardOwedFn(comet: String): (String) -> ContractCall {
        return { user ->
            createFunction(
                "getRewardOwed",
                listOf(comet.toAddress(), user.toAddress()),
                listOf(address(), uint256())
            )
        }
    }

    open suspend fun getRewardConfig(comet: String): RewardConfig {
        return (read(
            "rewardConfig",
            inputs = listOf(comet.toAddress()),
            outputs = listOf(
                address(),
                uint64(),
                bool(),
                uint256(),
            )
        )[0].value as String).let {
            RewardConfig(it)
        }
    }

    data class RewardConfig(val token: String)

    fun claimFn(comet: String): (String) -> ContractCall {
        return { user ->
            createFunction(
                method = "claim",
                inputs = listOf(comet.toAddress(), user.toAddress(), true.toBool()),
            )
        }
    }
}