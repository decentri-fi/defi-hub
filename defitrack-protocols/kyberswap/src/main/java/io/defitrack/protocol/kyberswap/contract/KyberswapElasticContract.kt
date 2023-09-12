package io.defitrack.protocol.kyberswap.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import java.math.BigInteger

class KyberswapElasticContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(blockchainGateway, "", address) {

    suspend fun poolLength(): BigInteger {
        return readSingle("poolLength", TypeUtils.uint256())
    }

    suspend fun allPairs(): List<PoolInfo> {
        val functions = (0 until poolLength().toInt()).map { poolIndex ->
            createFunction(
                "pools",
                inputs = listOf(poolIndex.toBigInteger().toUint256()),
                outputs = listOf(
                    TypeUtils.address(),
                    TypeUtils.uint32(),
                    TypeUtils.uint32(),
                    TypeUtils.uint32(),
                    TypeUtils.uint256(),
                    TypeUtils.uint256(),
                    TypeUtils.uint256(),
                )
            )
        }

        return readMultiCall(functions).map {
            PoolInfo(
                address = it.data[0].value as String,
                startTime = it.data[1].value as BigInteger,
                endTime = it.data[2].value as BigInteger,
                vestingDuration = it.data[3].value as BigInteger,
                totalSecondsClaimed = it.data[4].value as BigInteger,
                feeTarget = it.data[5].value as BigInteger,
                numStakes = it.data[6].value as BigInteger,
            )
        }
    }

    class PoolInfo(
        val address: String,
        val startTime: BigInteger,
        val endTime: BigInteger,
        val vestingDuration: BigInteger,
        val totalSecondsClaimed: BigInteger,
        val feeTarget: BigInteger,
        val numStakes: BigInteger
    )
}