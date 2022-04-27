package io.defitrack.protocol.polycat

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway.Companion.toUint256
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint16
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class PolycatMasterChefContract(
    contractAccessor: BlockchainGateway,
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
            "fish",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    fun claimableAmount(poolIndex: Int, address: String): BigInteger {
        return read(
            "pendingFish",
            inputs = listOf(
                poolIndex.toBigInteger().toUint256(), address.toAddress()
            ),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun poolInfo(poolId: Int): PoolInfo {
        return poolInfos[poolId]
    }

    val poolInfos by lazy {
        val multicalls = (0 until poolLength).map { poolIndex ->
            MultiCallElement(
                createFunction(
                    "poolInfo",
                    inputs = listOf(poolIndex.toBigInteger().toUint256()),
                    outputs = listOf(
                        TypeReference.create(Address::class.java),
                        TypeReference.create(Uint256::class.java),
                        TypeReference.create(Uint256::class.java),
                        TypeReference.create(Uint256::class.java),
                        TypeReference.create(Uint16::class.java),
                    )
                ),
                this.address
            )
        }
        val results = blockchainGateway.readMultiCall(
            multicalls
        )
        results.map { retVal ->
            PoolInfo(
                retVal[0].value as String,
                retVal[1].value as BigInteger,
                retVal[2].value as BigInteger,
                retVal[3].value as BigInteger,
                retVal[4].value as BigInteger,
            )
        }
    }

    val rewardPerBlock by lazy {
        read(
            "fishPerBlock",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }


    val totalAllocPoint by lazy {
        read(
            "totalAllocPoint",
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