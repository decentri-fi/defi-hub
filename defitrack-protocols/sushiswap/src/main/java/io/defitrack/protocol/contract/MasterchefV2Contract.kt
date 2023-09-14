package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.abi.TypeUtils.Companion.uint64
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import kotlinx.coroutines.Deferred
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class MasterchefV2Contract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, address
) {

    fun harvestFunction(poolId: Int): Function {
        return createFunction(
            "withdraw",
            listOf(
                poolId.toBigInteger().toUint256(),
                BigInteger.ZERO.toUint256()
            ),
        )
    }

    fun pendingFunction(poolId: Int, user: String): Function {
        return createFunction(
            "pendingSushi",
            listOf(
                poolId.toBigInteger().toUint256(),
                user.toAddress()
            ),
            listOf(
                uint256()
            )
        )
    }

    suspend fun poolLength(): Int {
        return readSingle<BigInteger>("poolLength", uint256()).toInt()
    }

    suspend fun poolInfos(): List<MasterChefV2PoolInfo> {
        val functions = (0 until poolLength()).map { poolIndex ->
            createFunction(
                "poolInfo",
                inputs = listOf(poolIndex.toBigInteger().toUint256()),
                outputs = listOf(
                    uint128(),
                    uint64(),
                    uint64()
                )
            )
        }

        return this.readMultiCall(functions).map { retVal ->
            MasterChefV2PoolInfo(
                retVal.data[0].value as BigInteger,
                retVal.data[1].value as BigInteger,
                retVal.data[2].value as BigInteger,
            )
        }
    }

    suspend fun lpToken(poolId: Int): String {
        return read(
            "lpToken",
            listOf(poolId.toBigInteger().toUint256()),
            listOf(address())
        )[0].value as String
    }

    val rewardToken: Deferred<String> = lazyAsync {
        readSingle("SUSHI", address())
    }


    val totalAllocPoint: Deferred<BigInteger> = lazyAsync {
        readSingle("totalAllocPoint", uint256())
    }

    val perSecond: Deferred<BigInteger> = lazyAsync {
        readSingle("sushiPerBlock", uint256())
    }

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
}