package io.defitrack.protocol.radiant

import arrow.core.nel
import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class MasterChefContract(address: String) : EvmContract(address) {

    val poolLength = constant<BigInteger>("poolLength", uint256())
    suspend fun getPoolInfo(): List<PoolInfo> {
        return (0 until poolLength.await().toInt()).map {
            registeredTokens(it)
        }.map {
            poolInfo(it)
        }
    }

    private suspend fun registeredTokens(index: Int): String {
        return read(
            "registeredTokens",
            index.toUint256().nel(),
            TypeUtils.address().nel()
        )[0].value as String
    }

    private suspend fun poolInfo(underlying: String): PoolInfo {
        val retVal = read(
            "poolInfo",
            underlying.toAddress().nel(),
            listOf(
                uint256(),
                uint256(),
                uint256(),
                TypeUtils.address()
            )
        )

        return PoolInfo(
            underlying,
            retVal[0].value as BigInteger,
            retVal[1].value as BigInteger,
            retVal[2].value as BigInteger,
            retVal[3].value as String
        )
    }

    data class PoolInfo(
        val underlying: String,
        val allocPoint: BigInteger,
        val lastRewardTime: BigInteger,
        val accRewardPerShare: BigInteger,
        val onwardIncentives: String
    )

}