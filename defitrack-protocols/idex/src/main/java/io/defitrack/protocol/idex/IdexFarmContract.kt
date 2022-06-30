package io.defitrack.protocol.idex

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
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
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java)
            )
        )
    }


    suspend fun poolLength(): Int {
        return (readWithAbi(
            "poolLength",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger).toInt()
    }

    suspend fun rewardPerBlock(): BigInteger {
        return readWithAbi(
            "rewardTokenPerBlock",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    suspend fun getLpTokenForPoolId(poolIndex: Int): String {
        return readWithAbi(
            "poolInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256()),
            outputs = listOf(
                TypeReference.create(Address::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
            )
        )[0].value as String
    }

    suspend fun rewardToken(): String {
        return readWithAbi(
            "rewardToken",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }
}