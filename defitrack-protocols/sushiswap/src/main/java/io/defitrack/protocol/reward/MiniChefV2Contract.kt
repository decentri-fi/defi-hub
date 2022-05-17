package io.defitrack.protocol.reward

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway.Companion.toUint256
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint128
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint64
import java.math.BigInteger

class MiniChefV2Contract(
    blockchainGateway: BlockchainGateway,
    abi: String, address: String
) : EvmContract(blockchainGateway, abi, address) {

    fun accSushiPerShare(poolIndex: Int): BigInteger {
        return poolInfos[poolIndex].accSushiPerShare
    }

    fun userInfoFunction(poolId: Int, user: String): Function {
        return createFunction(
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


    val poolInfos: List<MinichefPoolInfo> by lazy {
        val multicalls = (0 until poolLength).map { poolIndex ->
            MultiCallElement(
                createFunction(
                    "poolInfo",
                    inputs = listOf(poolIndex.toBigInteger().toUint256()),
                    outputs = listOf(
                        TypeReference.create(Uint128::class.java),
                        TypeReference.create(Uint64::class.java),
                        TypeReference.create(Uint64::class.java),
                    )
                ),
                this.address
            )
        }

        val results = this.blockchainGateway.readMultiCall(
            multicalls
        )
        results.map { retVal ->
            MinichefPoolInfo(
                retVal[0].value as BigInteger,
                retVal[1].value as BigInteger,
                retVal[2].value as BigInteger,
            )
        }
    }

    fun poolInfo(poolIndex: Int): MinichefPoolInfo {
        return poolInfos[poolIndex]
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

    val lps: List<String> by lazy {
        val multicalls = (0 until poolLength).map { poolIndex ->
            MultiCallElement(
                createFunction(
                    "lpToken",
                    inputs = listOf(poolIndex.toBigInteger().toUint256()),
                    outputs = listOf(
                        TypeReference.create(Address::class.java),
                    )
                ),
                this.address
            )
        }
        val results = this.blockchainGateway.readMultiCall(multicalls)
        results.map { retVal ->
            retVal[0].value as String
        }
    }

    fun getLpTokenForPoolId(poolIndex: Int): String = lps[poolIndex]

    val rewardToken by lazy {
        read(
            "SUSHI",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }


    val sushiPerSecond by lazy {
        read(
            "sushiPerSecond",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    val totalAllocPoint by lazy {
        read(
            "totalAllocPoint",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }
}