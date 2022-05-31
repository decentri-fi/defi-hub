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
        val retVal: BigInteger = read("poolLength")
        retVal.toInt()
    }

    val totalAllocPoint: BigInteger by lazy {
        read("totalAllocPoint")
    }

    val rewardToken: String by lazy {
        read("spirit")
    }

    val sushiPerSecond: BigInteger by lazy {
        read("spiritPerBlock")
    }

    fun userInfoFunction(poolId: Int, user: String): Function {
        return createFunctionWithAbi(
            "userInfo",
            listOf(
                poolId.toBigInteger().toUint256(),
                user.toAddress()
            )
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
                retVal[4].value as BigInteger,
            )
        }
    }

    fun poolInfo(poolIndex: Int): PoolInfo {
        return poolInfos[poolIndex]
    }
}