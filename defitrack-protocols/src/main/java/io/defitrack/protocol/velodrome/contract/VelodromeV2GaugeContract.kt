package io.defitrack.protocol.velodrome.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import kotlinx.coroutines.Deferred

class VelodromeV2GaugeContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, address
) {

    val stakedToken: Deferred<String> = constant("stakingToken", address())

    val rewardToken: Deferred<String> = constant("rewardToken", address())

    fun getRewardFn(address: String): ContractCall {
        return createFunction(
            "getReward",
            listOf(address.toAddress())
        )
    }

    fun earnedFn(address: String) = createFunction("earned", listOf(address.toAddress()), listOf(uint256()))
}