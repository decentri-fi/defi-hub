package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function

class VelodromeV2GaugeContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, "", address
) {

    suspend fun stakedToken(): String {
        return readWithoutAbi(
            "stakingToken",
            inputs = listOf(),
            outputs = listOf(TypeUtils.address())
        )[0].value as String
    }


    suspend fun rewardToken(): String {
        return readWithoutAbi(
            "rewardToken",
            emptyList(),
            listOf(TypeUtils.address())
        )[0].value as String
    }

    fun getRewardFn(address: String): Function {
        return createFunction(
            "getReward",
            listOf(address.toAddress())
        )
    }

    fun earnedFn(address: String) = createFunction("earned", listOf(address.toAddress()), listOf(uint256()))
}