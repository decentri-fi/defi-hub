package io.defitrack.protocol.dinoswap

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway.Companion.toUint256
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class DinoswapFossilFarmsContract(
    contractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : EvmContract(
    contractAccessor, abi, address
) {

    val poolLength by lazy {
        (readWithAbi(
            "poolLength",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger).toInt()
    }

    val rewardToken by lazy {
        readWithAbi(
            "dino",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    fun claimableAmount(poolIndex: Int, address: String): BigInteger {
        return readWithAbi(
            "pendingDino",
            inputs = listOf(
                poolIndex.toBigInteger().toUint256(), address.toAddress()
            ),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun getLpTokenForPoolId(poolIndex: Int): String {
        return readWithAbi(
            "poolInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256()),
            outputs = listOf(
                TypeReference.create(Address::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as String
    }

    val rewardPerBlock by lazy {
        readWithAbi(
            "dinoPerBlock",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    fun userInfo(address: String, poolIndex: Int): UserInfo {
        val result = readWithAbi(
            "userInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256(), address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
            )
        )

        return UserInfo(
            amount = result[0].value as BigInteger,
            rewardDebt = result[1].value as BigInteger
        )
    }

    fun userInfoFunction(address: String, poolIndex: Int): Function {
        return createFunctionWithAbi(
            "userInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256(), address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
            )
        )
    }
}

data class UserInfo(
    val amount: BigInteger,
    val rewardDebt: BigInteger
)