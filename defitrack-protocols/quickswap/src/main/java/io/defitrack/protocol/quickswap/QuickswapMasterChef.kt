package io.defitrack.protocol.quickswap

import io.defitrack.evm.contract.*
import io.defitrack.evm.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.evm.contract.EvmContractAccessor.Companion.toUint256
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class QuickswapVaultChefContract(
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

    val totalSupply by lazy {
        (read(
            "totalSupply",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        ))[0].value as BigInteger
    }

    val rewardTokenAddress by lazy {
        read(
            "rewardToken",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    fun claimableAmount(poolIndex: Int, address: String): BigInteger {
        return read(
            "pendingReward",
            inputs = listOf(
                poolIndex.toBigInteger().toUint256(), address.toAddress()
            ),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun strategy(poolIndex: Int): String {
        return read(
            "poolInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256()),
            outputs = listOf(
                TypeReference.create(Address::class.java),
                TypeReference.create(Address::class.java),
            )
        )[1].value as String
    }

    fun getLpTokenAddressForPoolId(poolIndex: Int): String {
        return read(
            "poolInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256()),
            outputs = listOf(
                TypeReference.create(Address::class.java),
                TypeReference.create(Address::class.java),
            )
        )[0].value as String
    }

    fun userInfo(address: String, poolIndexes: List<Int>): List<UserInfo> {
        return readMultiple(poolIndexes.map {
            ReadRequest(
                "userInfo",
                inputs = listOf(it.toBigInteger().toUint256(), address.toAddress()),
                outputs = listOf(
                    TypeReference.create(Uint256::class.java),
                )
            )
        }).map {
            UserInfo(
                amount = it[0].value as BigInteger
            )
        }
    }

    fun userInfo(address: String, poolIndex: Int): UserInfo {
        val result = read(
            "userInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256(), address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java),
            )
        )

        return UserInfo(
            amount = result[0].value as BigInteger
        )
    }
}

data class UserInfo(
    val amount: BigInteger,
)

class IStrategy(
    solidityBasedContractAccessor: EvmContractAccessor,
    abi: String,
    address: String,
) : EvmContract(
    solidityBasedContractAccessor,
    abi, address
) {

    val wantLockedTotal by lazy {
        read(
            "wantLockedTotal",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    val sharesTotal by lazy {
        read(
            "sharesTotal",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

}