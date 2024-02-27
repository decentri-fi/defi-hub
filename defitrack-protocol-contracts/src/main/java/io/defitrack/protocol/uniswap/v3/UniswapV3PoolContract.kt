package io.defitrack.uniswap.v3

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.bool
import io.defitrack.abi.TypeUtils.Companion.int128
import io.defitrack.abi.TypeUtils.Companion.int24
import io.defitrack.abi.TypeUtils.Companion.int56
import io.defitrack.abi.TypeUtils.Companion.toInt24
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint16
import io.defitrack.abi.TypeUtils.Companion.uint160
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.abi.TypeUtils.Companion.uint32
import io.defitrack.abi.TypeUtils.Companion.uint8
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import kotlinx.coroutines.Deferred
import java.math.BigInteger

context(BlockchainGateway)
class UniswapV3PoolContract(
    address: String
) : ERC20Contract(
     address
) {

    val liquidity = constant<BigInteger>("liquidity", uint128())

    val slot0 = constant<Slot0>(
        "slot0", listOf(
            uint160(),
            int24(),
            uint16(),
            uint16(),
            uint16(),
            uint8(),
            bool()
        )
    ) {
        Slot0(
            tick = it[1].value as BigInteger,
            sqrtPriceX96 = it[0].value as BigInteger
        )
    }

    suspend fun refreshLiquidity(): BigInteger {
        return readSingle("liquidity", uint128())
    }

    val token0: Deferred<String> = constant<String>("token0", TypeUtils.address())
    val token1: Deferred<String> = constant<String>("token1", TypeUtils.address())


    val feeGrowthGlobal0X128 = constant<BigInteger>("feeGrowthGlobal0X128", uint256())

    val feeGrowthGlobal1X128 = constant<BigInteger>("feeGrowthGlobal1X128", uint256())

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
                bool()
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
        val sqrtPriceX96: BigInteger,
        val tick: BigInteger
    )
}