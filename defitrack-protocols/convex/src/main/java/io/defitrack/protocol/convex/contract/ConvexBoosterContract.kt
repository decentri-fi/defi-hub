package io.defitrack.protocol.convex.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.bool
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import java.math.BigInteger

class ConvexBoosterContract(
    blockchainGateway: BlockchainGateway,
    abi: String,
    address: String
) : EvmContract(blockchainGateway, abi, address) {

    suspend fun poolLength(): Int {
        return readSingle<BigInteger>("poolLength", uint256()).toInt()
    }

    suspend fun poolInfos(): List<PoolInfo> {
        val functions = (0 until poolLength()).map { poolIndex ->
            createFunctionWithAbi(
                "poolInfo",
                inputs = listOf(poolIndex.toBigInteger().toUint256()),
                outputs = listOf(
                    address(),
                    address(),
                    address(),
                    address(),
                    address(),
                    bool(),
                )
            )
        }

        val results = readMultiCall(functions)
        return results.map { retVal ->
            PoolInfo(
                retVal.data[0].value as String,
                retVal.data[1].value as String,
                retVal.data[2].value as String,
                retVal.data[3].value as String,
                retVal.data[4].value as String,
                retVal.data[5].value as Boolean,
            )
        }
    }

}

class PoolInfo(
    val lpToken: String,
    val token: String,
    val gauge: String,
    val crvRewards: String,
    val stash: String,
    val shutDown: Boolean
)