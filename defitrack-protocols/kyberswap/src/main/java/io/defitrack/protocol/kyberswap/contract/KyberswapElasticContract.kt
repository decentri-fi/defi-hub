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
        return readWithoutAbi(
            "poolLength",
            outputs = listOf(TypeUtils.uint256())
        )[0].value as BigInteger
    }

    suspend fun allPairs(): List<PoolInfo> {
        val multicalls = (0 until poolLength().toInt()).map { poolIndex ->
            MultiCallElement(
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
                ),
                this.address
            )
        }

        val results = this.blockchainGateway.readMultiCall(
            multicalls
        )

        return results.map {
            PoolInfo(
                address = it[0].value as String,
                startTime = it[1].value as BigInteger,
                endTime = it[2].value as BigInteger,
                vestingDuration = it[2].value as BigInteger,
                totalSecondsClaimed = it[2].value as BigInteger,
                feeTarget = it[2].value as BigInteger,
                numStakes = it[2].value as BigInteger,
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