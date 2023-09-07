package io.defitrack.protocol.reward

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.abi.TypeUtils.Companion.uint64
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import kotlinx.coroutines.Deferred
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class MiniChefV2Contract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(blockchainGateway, "", address) {

    fun userInfoFunction(poolId: Int, user: String): Function {
        return createFunction(
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

    fun harvestFunction(pid: Int, to: String): Function {
        return createFunction(
            "harvest",
            listOf(
                pid.toBigInteger().toUint256(),
                to.toAddress()
            )
        )
    }


    suspend fun poolInfos(): List<MinichefPoolInfo> {
        val multicalls = (0 until poolLength()).map { poolIndex ->
            MultiCallElement(
                createFunction(
                    "poolInfo",
                    inputs = listOf(poolIndex.toBigInteger().toUint256()),
                    outputs = listOf(
                        uint128(),
                        uint64(),
                        uint64(),
                    )
                ),
                this.address
            )
        }

        val results = this.blockchainGateway.readMultiCall(
            multicalls
        )
        return results.map { retVal ->
            MinichefPoolInfo(
                retVal[0].value as BigInteger,
                retVal[1].value as BigInteger,
                retVal[2].value as BigInteger,
            )
        }
    }

    suspend fun poolInfo(poolIndex: Int): MinichefPoolInfo {
        return poolInfos()[poolIndex]
    }

    suspend fun poolLength(): Int {
        return readSingle<BigInteger>("poolLength", uint256()).toInt()
    }

    val lps: Deferred<List<String>> = lazyAsync {
        val multicalls = (0 until poolLength()).map { poolIndex ->
            MultiCallElement(
                createFunction(
                    "lpToken",
                    inputs = listOf(poolIndex.toBigInteger().toUint256()),
                    outputs = listOf(address())
                ),
                address
            )
        }
        val results = blockchainGateway.readMultiCall(multicalls)
        results.map { retVal ->
            retVal[0].value as String
        }
    }

    suspend fun getLpTokenForPoolId(poolIndex: Int): String = lps.await()[poolIndex]

    val rewardToken: Deferred<String> = lazyAsync {
        readSingle("SUSHI", address())
    }

    fun pendingSushiFunction(pid: Int, address: String): Function {
        return createFunction(
            "pendingSushi",
            inputs = listOf(
                pid.toBigInteger().toUint256(),
                address.toAddress()
            ),
            outputs = listOf(uint256())
        )
    }


    val  sushiPerSecond: Deferred<BigInteger> = lazyAsync {
        readSingle("sushiPerSecond", uint256())
    }

    val totalAllocPoint: Deferred<BigInteger> = lazyAsync {
        readSingle("totalAllocPoint", uint256())
    }
}