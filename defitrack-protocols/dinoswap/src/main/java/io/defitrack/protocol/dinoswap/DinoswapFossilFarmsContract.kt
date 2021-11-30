package io.defitrack.protocol.dinoswap

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toUint256
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint16
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class DinoswapFossilFarmsContract(
    contractAccessor: EvmContractAccessor,
    abi: String,
    address: String,
) : EvmContract(
    contractAccessor, abi, address
) {

    val poolLength by lazy {
        (read(
            "poolLength",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger).toInt()
    }

    val rewardToken by lazy {
        read(
            "dino",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    fun claimableAmount(poolIndex: Int, address: String): BigInteger {
        return read(
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
        return read(
            "poolInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256()),
            outputs = listOf(
                TypeReference.create(Address::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint16::class.java),
            )
        )[0].value as String
    }

    val rewardPerBlock by lazy {
        read(
            "dinoPerBlock",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    fun userInfo(address: String, poolIndex: Int): UserInfo {
        val result = read(
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
}

data class UserInfo(
    val amount: BigInteger,
    val rewardDebt: BigInteger
)