package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

class VelodromeGaugeContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, "", address
) {

    suspend fun stakedToken(): String {
        return readWithoutAbi(
            "stake",
            inputs = listOf(),
            outputs = listOf(TypeUtils.address())
        )[0].value as String
    }

    suspend fun getRewardListLength(): BigInteger {
        return readWithoutAbi(
            "rewardsListLength",
            listOf(),
            listOf(uint256())
        )[0].value as BigInteger
    }

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