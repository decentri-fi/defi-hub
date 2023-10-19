package io.defitrack.protocol.polycat.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint16
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.protocol.sushiswap.contract.MasterChefBasedContract
import kotlinx.coroutines.Deferred
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class PolycatMasterChefContract(
    contractAccessor: BlockchainGateway,
    address: String,
) : MasterChefBasedContract(
    "fish",
    "fishPerBlock",
    "pendingFish",
    contractAccessor,
    address
) {


    val poolInfos: Deferred<List<PoolInfo>> = lazyAsync {
        val functions = (0 until poolLength.await().toInt()).map { poolIndex ->
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

        readMultiCall(functions).map { retVal ->
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
        return poolInfos.await()[poolId]
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