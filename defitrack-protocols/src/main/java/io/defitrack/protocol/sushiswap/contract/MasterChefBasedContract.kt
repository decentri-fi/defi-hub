package io.defitrack.protocol.sushiswap.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.multicall.MultiCallElement
import kotlinx.coroutines.Deferred
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

open class MasterChefBasedContract(
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

    val poolLength = constant<BigInteger>("poolLength", uint256())

    val defaultPoolInfos: Deferred<List<MasterChefPoolInfo>> = lazyAsync {
        val multicalls = (0 until poolLength.await().toInt()).map { poolIndex ->
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
                address
            )
        }

        val results = blockchainGateway.readMultiCall(
            multicalls
        )
        results.map { retVal ->
            MasterChefPoolInfo(
                retVal.data[0].value as String,
                retVal.data[1].value as BigInteger,
                retVal.data[2].value as BigInteger,
                retVal.data[3].value as BigInteger,
            )
        }
    }

    suspend fun getLpTokenForPoolId(poolIndex: Int): MasterChefPoolInfo = defaultPoolInfos.await()[poolIndex]

    val rewardToken: Deferred<String> = lazyAsync {
        readSingle(rewardTokenName, address())
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