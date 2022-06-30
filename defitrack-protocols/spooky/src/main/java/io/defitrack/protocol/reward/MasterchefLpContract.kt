package io.defitrack.protocol.reward

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class MasterchefLpContract(
    blockchainGateway: BlockchainGateway,
    abi: String,
    address: String
) : EvmContract(
    blockchainGateway, abi, address
) {

    suspend fun poolLength(): Int {
        return (readWithAbi(
            "poolLength"
        )[0].value as BigInteger).toInt()
    }

    suspend fun totalAllocPoint(): BigInteger {
        return (readWithAbi(
            "totalAllocPoint",
        )[0].value as BigInteger)
    }

    suspend fun rewardToken(): String {
        return readWithAbi("boo")[0].value as String
    }

    suspend fun sushiPerSecond(): BigInteger {
        return readWithAbi("booPerSecond")[0].value as BigInteger
    }

    fun userInfoFunction(poolId: Int, address: String): Function {
        return createFunctionWithAbi(
            "userInfo",
            inputs = listOf(
                poolId.toBigInteger().toUint256(),
                address.toAddress()
            ),
        )
    }

    suspend fun poolInfos(): List<PoolInfo> {
        val multicalls = (0 until poolLength()).map { poolIndex ->
            MultiCallElement(
                createFunctionWithAbi(
                    "poolInfo",
                    inputs = listOf(poolIndex.toBigInteger().toUint256()),
                ),
                this.address
            )
        }

        val results = this.blockchainGateway.readMultiCall(
            multicalls
        )
        return results.map { retVal ->
            PoolInfo(
                retVal[0].value as String,
                retVal[1].value as BigInteger,
                retVal[2].value as BigInteger,
                retVal[3].value as BigInteger,
            )
        }
    }

    suspend fun poolInfo(poolIndex: Int): PoolInfo {
        return poolInfos()[poolIndex]
    }
}