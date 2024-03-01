package io.defitrack.protocol.aura

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.bool
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class AuraBoosterContract : EvmContract("0xa57b8d98dae62b26ec3bcc4a365338157060b234") {

    suspend fun poolLength(): Int {
        return readSingle<BigInteger>("poolLength", uint256()).toInt()
    }

    suspend fun poolInfos(): List<PoolInfo> {
        val functions = (0 until poolLength()).map { poolIndex ->
            createFunction(
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

        return readMultiCall(functions).map { retVal ->
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

    data class PoolInfo(
        val lpToken: String,
        val token: String,
        val gauge: String,
        val crvRewards: String,
        val stash: String,
        val isStaking: Boolean,
    )
}