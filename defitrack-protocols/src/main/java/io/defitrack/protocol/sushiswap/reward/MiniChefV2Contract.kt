package io.defitrack.protocol.sushiswap.reward

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.abi.TypeUtils.Companion.uint64
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import kotlinx.coroutines.Deferred
import java.math.BigInteger

class MiniChefV2Contract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(blockchainGateway, address) {

    fun userInfoFunction(poolId: Int): (String) -> ContractCall {
        return { user: String ->
            createFunction(
                "userInfo",
                listOf(
                    poolId.toBigInteger().toUint256(),
                    user.toAddress()
                ),
                listOf(
                    uint256(),
                    uint256()
                )
            )
        }
    }

    fun harvestFunction(pid: Int): (String) -> ContractCall {
        return { user: String ->
            createFunction(
                "harvest",
                listOf(
                    pid.toBigInteger().toUint256(),
                    user.toAddress()
                )
            )
        }
    }


    suspend fun poolInfos(): List<MinichefPoolInfo> {
        val multicalls = (0 until poolLength()).map { poolIndex ->
            createFunction(
                "poolInfo",
                inputs = listOf(poolIndex.toBigInteger().toUint256()),
                outputs = listOf(
                    uint128(),
                    uint64(),
                    uint64(),
                )
            )
        }

        val results = this.blockchainGateway.readMultiCall(
            multicalls
        )
        return results.map { retVal ->
            MinichefPoolInfo(
                retVal.data[0].value as BigInteger,
                retVal.data[1].value as BigInteger,
                retVal.data[2].value as BigInteger,
            )
        }
    }


    suspend fun poolLength(): Int {
        return readSingle<BigInteger>("poolLength", uint256()).toInt()
    }

    val lps: Deferred<List<String>> = lazyAsync {
        val multicalls = (0 until poolLength()).map { poolIndex ->
            createFunction(
                "lpToken",
                inputs = listOf(poolIndex.toBigInteger().toUint256()),
                outputs = listOf(address())
            )
        }
        val results = blockchainGateway.readMultiCall(multicalls)
        results.map { retVal ->
            retVal.data[0].value as String
        }
    }

    suspend fun getLpTokenForPoolId(poolIndex: Int): String = lps.await()[poolIndex]

    val rewardToken: Deferred<String> = lazyAsync {
        readSingle("SUSHI", address())
    }

    fun pendingSushiFunction(pid: Int): (String) -> ContractCall {
        return { user: String ->
            createFunction(
                "pendingSushi",
                inputs = listOf(
                    pid.toBigInteger().toUint256(),
                    user.toAddress()
                ),
                outputs = listOf(uint256())
            )
        }
    }
}