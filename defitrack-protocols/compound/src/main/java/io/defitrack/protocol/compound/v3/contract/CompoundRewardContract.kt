package io.defitrack.protocol.compound.v3.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toBool
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Uint
import java.math.BigInteger

class CompoundRewardContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    fun getRewardOwedFn(comet: String, user: String): Function {
        return createFunction(
            "getRewardOwed",
            listOf(comet.toAddress(), user.toAddress()),
            listOf(TypeUtils.address(), uint256())
        )
    }

    fun claimFn(comet: String, user: String): Function {
        return createFunction(
            method = "claim",
            inputs = listOf(comet.toAddress(), user.toAddress(), true.toBool()),
        )
    }

    class RewardOwed : DynamicStruct {
        val rewardOwed: String
        val owed: BigInteger

        constructor(_rewardOwed: String, _owed: BigInteger) : super(_rewardOwed.toAddress(), _owed.toUint256()) {
            rewardOwed = _rewardOwed
            owed = _owed
        }

        constructor(rewardOwed: Address, owed: Uint) {
            this.rewardOwed = rewardOwed.value
            this.owed = owed.value
        }
    }
}