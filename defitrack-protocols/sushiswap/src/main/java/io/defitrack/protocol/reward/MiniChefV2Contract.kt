package io.defitrack.protocol.reward

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toUint256
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint128
import org.web3j.abi.datatypes.generated.Uint16
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint64
import java.math.BigInteger

class MiniChefV2Contract(
    private val evmContractAccessor: EvmContractAccessor,
    abi: String, address: String
) : EvmContract(evmContractAccessor, abi, address) {


    fun accSushiPerShare(poolIndex: Int): BigInteger {
        return read(
            "poolInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256()),
            outputs = listOf(
                TypeReference.create(Uint128::class.java),
                TypeReference.create(Uint64::class.java),
                TypeReference.create(Uint64::class.java),
            )
        )[0].value as BigInteger
    }

    val poolLength by lazy {
        (read(
            "poolLength",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger).toInt()
    }

    fun claimableAmount(poolIndex: Int, address: String): BigInteger {
        return read(
            "pendingSushi",
            inputs = listOf(
                poolIndex.toBigInteger().toUint256(), address.toAddress()
            ),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    val rewardPerBlock by lazy {
        read(
            "sushiPerSecond",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    fun getLpTokenForPoolId(poolIndex: Int): String {
        return read(
            "lpToken",
            inputs = listOf(poolIndex.toBigInteger().toUint256()),
            outputs = listOf(
                TypeReference.create(Address::class.java),
            )
        )[0].value as String
    }


    val rewardToken by lazy {
        read(
            "SUSHI",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }
}