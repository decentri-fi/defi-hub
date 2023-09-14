package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

class VelodromeV1GaugeContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, address
) {

    suspend fun stakedToken(): String {
        return readSingle("stake", TypeUtils.address())
    }

    suspend fun getRewardListLength(): BigInteger {
        return readSingle("rewardsListLength", uint256())
    }

    //Todo: multicall it
    suspend fun getRewardList(): List<String> {
        return (0 until getRewardListLength().toInt()).map {
            readWithoutAbi(
                "rewards",
                listOf(it.toBigInteger().toUint256()),
                listOf(TypeUtils.address())
            )[0].value as String
        }
    }
}