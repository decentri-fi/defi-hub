package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import kotlinx.coroutines.Deferred
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class MasterChefBasedContract(
    private val rewardTokenName: String,
    private val perSecondName: String,
    private val pendingName: String,
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
            pendingName,
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
        return (readSingle<BigInteger>(
            "poolLength", uint256()
        )).toInt()
    }

    suspend fun poolInfos(): List<MasterChefPoolInfo> {
        val multicalls = (0 until poolLength()).map { poolIndex ->
            MultiCallElement(
                createFunction(
                    "poolInfo",
                    inputs = listOf(poolIndex.toBigInteger().toUint256()),
                    outputs = listOf(
                        address(),
                        uint256(),
                        uint256(),
                        uint256()
                    )
                ),
                this.address
            )
        }

        val results = this.blockchainGateway.readMultiCall(
            multicalls
        )
        return results.map { retVal ->
            MasterChefPoolInfo(
                retVal.data[0].value as String,
                retVal.data[1].value as BigInteger,
                retVal.data[2].value as BigInteger,
                retVal.data[3].value as BigInteger,
            )
        }
    }

    suspend fun getLpTokenForPoolId(poolIndex: Int): MasterChefPoolInfo = poolInfos()[poolIndex]

    val rewardToken: Deferred<String> = lazyAsync {
        readSingle(rewardTokenName, address())
    }


    val totalAllocPoint: Deferred<BigInteger> = lazyAsync {
        readSingle("totalAllocPoint", uint256())
    }

    val perSecond: Deferred<BigInteger> = lazyAsync {
        readSingle(perSecondName, uint256())
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