package io.defitrack.protocol.autoearn

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.protocol.sushiswap.contract.MasterChefBasedContract
import java.math.BigInteger

context(BlockchainGateway)
class AutoEarnVaultContract(
    address: String
) : MasterChefBasedContract(
    "sushi",
    "pendingToken",
    address
) {

    fun getRewardFn(pid: Int): (String) -> ContractCall {
        return { _: String ->
            createFunction(
                "withdraw",
                listOf(
                    pid.toBigInteger().toUint256(),
                    BigInteger.ZERO.toUint256(),
                )
            )
        }
    }

    suspend fun poolInfos2(): List<PoolInfo> {
        val multicalls = (0 until poolLength.await().toInt()).map { poolIndex ->
            createFunction(
                "poolInfo2",
                inputs = listOf(poolIndex.toBigInteger().toUint256()),
                outputs = listOf(
                    TypeUtils.address(),
                    TypeUtils.uint256(),
                    TypeUtils.address(),
                    TypeUtils.uint256(),
                    TypeUtils.uint256(),
                    TypeUtils.address(),
                    TypeUtils.uint256(),
                    TypeUtils.uint256()
                )
            )
        }

        val results = readMultiCall(multicalls)

        return results.map { retVal ->
            PoolInfo(
                retVal.data[0].value as String,
            )
        }
    }

    fun autoEarnUserInfoFunction(poolId: Int): (String) -> ContractCall {
        return { user ->
            createFunction(
                "userInfo2",
                listOf(
                    poolId.toBigInteger().toUint256(),
                    user.toAddress()
                ),
                listOf(
                    TypeUtils.uint256(),
                    TypeUtils.uint256(),
                    TypeUtils.uint256(),
                    TypeUtils.uint256(),
                )
            )
        }
    }

    data class PoolInfo(
        val lpToken: String
    )
}