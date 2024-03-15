package io.defitrack.protocol.stargate.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class LPStakingContract(
    blockchainGateway: BlockchainGateway,
    address: String,
    private val pendingFunctionName: String,
    private val emissionTokenName: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    suspend fun emissionToken(): String {
        return readSingle(emissionTokenName, address())
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
                    uint256()
                )
            )
        }

        return readMultiCall(functions).map { retVal ->
            PoolInfo(
                retVal.data[0].value as String,
                retVal.data[1].value as BigInteger,
                retVal.data[2].value as BigInteger,
                retVal.data[3].value as BigInteger,
            )
        }
    }

    class PoolInfo(
        val lpToken: String,
        val allocPoint: BigInteger,
        val lastRewardBlock: BigInteger,
        val accStargatePerShare: BigInteger
    )


    suspend fun poolLength(): Int {
        return readSingle<BigInteger>("poolLength", uint256()).toInt()
    }

    fun userInfo(poolId: Int): (String) -> ContractCall {
        return { user: String ->
            createFunction(
                "userInfo",
                inputs = listOf(
                    poolId.toBigInteger().toUint256(),
                    user.toAddress()
                ),
                outputs = listOf(
                    uint256(),
                    uint256()
                )
            )
        }
    }

    fun pendingFn(poolId: Int): (String) -> ContractCall {
        return { user ->
            createFunction(
                pendingFunctionName,
                inputs = listOf(
                    poolId.toBigInteger().toUint256(),
                    user.toAddress()
                ),
                outputs = listOf(
                    uint256()
                )
            )
        }
    }

    fun claimFn(poolId: Int): (String) -> ContractCall {
        return { user: String ->
            createFunction(
                "deposit",
                listOf(poolId.toBigInteger().toUint256(), BigInteger.ZERO.toUint256())
            )
        }
    }
}