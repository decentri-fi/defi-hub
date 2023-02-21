package io.defitrack.protocol.polycat.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint16
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class PolycatMasterChefContract(
    contractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : EvmContract(
    contractAccessor, abi, address
) {

    suspend fun poolLength(): Int {
        return (readWithAbi(
            "poolLength",
            outputs = listOf(uint256())
        )[0].value as BigInteger).toInt()
    }

    suspend fun rewardToken(): String {
        return readWithAbi(
            "fish",
            outputs = listOf(address())
        )[0].value as String
    }

    suspend fun poolInfos(): List<PoolInfo> {
        val multicalls = (0 until poolLength()).map { poolIndex ->
            MultiCallElement(
                createFunctionWithAbi(
                    "poolInfo",
                    inputs = listOf(poolIndex.toBigInteger().toUint256()),
                    outputs = listOf(
                        address(),
                        uint256(),
                        uint256(),
                        uint256(),
                        uint16(),
                    )
                ),
                this.address
            )
        }
        val results = blockchainGateway.readMultiCall(
            multicalls
        )
        return results.map { retVal ->
            PoolInfo(
                retVal[0].value as String,
                retVal[1].value as BigInteger,
                retVal[2].value as BigInteger,
                retVal[3].value as BigInteger,
                retVal[4].value as BigInteger,
            )
        }
    }

    suspend fun poolInfo(poolId: Int): PoolInfo {
        return poolInfos()[poolId]
    }

    suspend fun rewardPerBlock(): BigInteger {
        return readWithAbi(
            "fishPerBlock",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }


    suspend fun totalAllocPoint(): BigInteger {
        return readWithAbi(
            "totalAllocPoint",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    fun userInfoFunction(user: String, poolIndex: Int): Function {
        return createFunctionWithAbi(
            "userInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256(), user.toAddress()),
            outputs = listOf(
                uint256(),
                uint256(),
            )
        )
    }

    suspend fun userInfo(address: String, poolIndex: Int): UserInfo {
        val result = readWithAbi(
            "userInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256(), address.toAddress()),
            outputs = listOf(
                uint256(),
                uint256(),
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