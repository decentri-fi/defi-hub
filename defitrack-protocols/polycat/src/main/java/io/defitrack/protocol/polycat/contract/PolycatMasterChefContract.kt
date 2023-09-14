package io.defitrack.protocol.polycat.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint16
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
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
        return readSingle("poolLength", uint256())
    }

    suspend fun rewardToken(): String {
        return readSingle("fish", address())
    }

    suspend fun poolInfos(): List<PoolInfo> {
        val functions = (0 until poolLength()).map { poolIndex ->
            createFunction(
                "poolInfo",
                inputs = listOf(poolIndex.toBigInteger().toUint256()),
                outputs = listOf(
                    address(),
                    uint256(),
                    uint256(),
                    uint256(),
                    uint16(),
                )
            )
        }

        return readMultiCall(functions).map { retVal ->
            PoolInfo(
                retVal.data[0].value as String,
                retVal.data[1].value as BigInteger,
                retVal.data[2].value as BigInteger,
                retVal.data[3].value as BigInteger,
                retVal.data[4].value as BigInteger,
            )
        }
    }

    suspend fun poolInfo(poolId: Int): PoolInfo {
        return poolInfos()[poolId]
    }

    suspend fun rewardPerBlock(): BigInteger {
        return readSingle("fishPerBlock", uint256())
    }


    suspend fun totalAllocPoint(): BigInteger {
        return readSingle("totalAllocPoint", uint256())
    }

    fun userInfoFunction(user: String, poolIndex: Int): Function {
        return createFunction(
            "userInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256(), user.toAddress()),
            outputs = listOf(
                uint256(),
                uint256(),
            )
        )
    }
}