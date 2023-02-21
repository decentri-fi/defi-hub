package io.defitrack.protocol.idex

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class IdexFarmContract(
    blockchainGateway: BlockchainGateway,
    abi: String, address: String
) : EvmContract(blockchainGateway, abi, address) {


    fun userInfoFunction(poolId: Int, user: String): Function {
        return createFunctionWithAbi(
            "userInfo",
            listOf(
                poolId.toBigInteger().toUint256(),
                user.toAddress()
            ),
            listOf(
                uint256(),
                uint256(),
            )
        )
    }


    suspend fun poolLength(): Int {
        return (readWithAbi(
            "poolLength",
            outputs = listOf(uint256())
        )[0].value as BigInteger).toInt()
    }

    suspend fun rewardPerBlock(): BigInteger {
        return readWithAbi(
            "rewardTokenPerBlock",
            outputs = listOf(
                uint256()
            )
        )[0].value as BigInteger
    }

    suspend fun getLpTokenForPoolId(poolIndex: Int): String {
        return readWithAbi(
            "poolInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256()),
            outputs = listOf(
                address(),
                uint256(),
                uint256(),
                uint256()
            )
        )[0].value as String
    }

    suspend fun rewardToken(): String {
        return readWithAbi(
            "rewardToken",
            outputs = listOf(address())
        )[0].value as String
    }
}