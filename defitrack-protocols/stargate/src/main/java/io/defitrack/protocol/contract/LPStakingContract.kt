package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class LPStakingContract(
    blockchainGateway: BlockchainGateway,
    abi: String, address: String
) : EvmContract(
    blockchainGateway, abi, address
) {

    suspend fun stargate(): String {
        return readWithAbi("stargate")[0].value as String
    }

    suspend fun lpBalances(index: Int): BigInteger {
        return readWithAbi(
            "lpBalances",
            inputs = listOf(index.toBigInteger().toUint256()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun poolInfos(): List<PoolInfo> {

        val multicalls = (0 until poolLength()).map { poolIndex ->
            MultiCallElement(
                createFunctionWithAbi(
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
            PoolInfo(
                retVal[0].value as String,
                retVal[1].value as BigInteger,
                retVal[2].value as BigInteger,
                retVal[3].value as BigInteger,
            )
        }
    }

    class PoolInfo(
        val lpToken: String,
        val allocPoint: BigInteger,
        val lastRewardBlock: BigInteger,
        val accStargatePerShare: BigInteger
    )


    suspend fun poolLength(): Int {
        return (readWithAbi(
            "poolLength",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger).toInt()
    }

    fun userInfo(poolId: Int, user: String): Function {
        return createFunction(
            "userInfo",
            inputs = listOf(
                poolId.toBigInteger().toUint256(),
                user.toAddress()
            ),
            outputs = listOf(
                uint256(),
                uint256()
            )
        )
    }
}