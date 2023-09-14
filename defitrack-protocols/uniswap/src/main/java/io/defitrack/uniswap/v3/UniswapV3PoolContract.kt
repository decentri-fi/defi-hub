package io.defitrack.uniswap.v3

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.int128
import io.defitrack.abi.TypeUtils.Companion.int56
import io.defitrack.abi.TypeUtils.Companion.toInt24
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint160
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.abi.TypeUtils.Companion.uint32
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import kotlinx.coroutines.Deferred
import java.math.BigInteger

class UniswapV3PoolContract(
    blockchaingateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchaingateway, "", address
) {

    suspend fun slot0(): Slot0 {
        val retVal = read(
            "slot0",
            listOf(),
            listOf(
                TypeUtils.uint160(),
                TypeUtils.int24(),
                TypeUtils.uint16(),
                TypeUtils.uint16(),
                TypeUtils.uint16(),
                TypeUtils.uint8(),
                TypeUtils.bool(),
            )
        )

        return Slot0(
            tick = retVal[1].value as BigInteger
        )
    }

    suspend fun token0(): String {
        return readSingle("token0", TypeUtils.address())
    }

    suspend fun token1(): String {
        return readSingle("token1", TypeUtils.address())
    }

    val  feeGrowthGlobal0X128: Deferred<BigInteger> = lazyAsync {
        readSingle("feeGrowthGlobal0X128", uint256())
    }

    val feeGrowthGlobal1X128: Deferred<BigInteger> = lazyAsync {
        readSingle("feeGrowthGlobal1X128", uint256())
    }

    suspend fun ticks(tick: BigInteger): Ticks {
        val retVal = read(
            "ticks",
            listOf(tick.toInt24()),
            listOf(
                uint128(),
                int128(),
                uint256(),
                uint256(),
                int56(),
                uint160(),
                uint32(),
                TypeUtils.bool()
            )
        )

        return Ticks(
            retVal[2].value as BigInteger,
            retVal[3].value as BigInteger,
        )
    }

    data class Ticks(
        val feeGrowthOutside0X128: BigInteger,
        val feeGrowthOutside1X128: BigInteger,
    )

    data class Slot0(
        val tick: BigInteger
    )
}