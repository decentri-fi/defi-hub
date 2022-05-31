package io.defitrack.protocol.reward

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway.Companion.toUint256
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

    val poolLength by lazy {
        (readWithAbi(
            "poolLength"
        )[0].value as BigInteger).toInt()
    }

    val totalAllocPoint by lazy {
        (readWithAbi(
            "totalAllocPoint",
        )[0].value as BigInteger)
    }


    val rewardToken by lazy {
        readWithAbi("boo")[0].value as String
    }

    val sushiPerSecond by lazy {
        readWithAbi("booPerSecond")[0].value as BigInteger
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

    val poolInfos: List<PoolInfo> by lazy {
        val multicalls = (0 until poolLength).map { poolIndex ->
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
        results.map { retVal ->
            PoolInfo(
                retVal[0].value as String,
                retVal[1].value as BigInteger,
                retVal[2].value as BigInteger,
                retVal[3].value as BigInteger,
            )
        }
    }

    fun poolInfo(poolIndex: Int): PoolInfo {
        return poolInfos[poolIndex]
    }
}