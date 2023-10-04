package io.defitrack.protocol.stargate.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class LPStakingContract(
    blockchainGateway: BlockchainGateway,
    address: String,
    private val pendingFunctionName: String,
    private val emissionTokenName: String
) : EvmContract(
    blockchainGateway, address
) {

    suspend fun emissionToken(): String {
        return readSingle(emissionTokenName, address())
    }

    suspend fun lpBalances(index: Int): BigInteger {
        return read(
            "lpBalances",
            inputs = listOf(index.toBigInteger().toUint256()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
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

    fun userInfo(poolId: Int, user: String): Function {
        return createFunction(
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

    fun pendingFn(poolId: Int, user: String): Function {
        return createFunction(
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

    fun claimFn(poolId: Int): Function {
        return createFunction(
            "deposit",
            listOf(poolId.toBigInteger().toUint256(), BigInteger.ZERO.toUint256())
        )
    }
}