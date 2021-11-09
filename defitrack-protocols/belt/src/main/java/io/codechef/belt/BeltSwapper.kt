package io.defitrack.belt

import io.defitrack.protocol.Swapper
import java.math.BigInteger

class BeltSwapper(private val beltSwapContract: BeltSwapContract) : Swapper {

    val reserves = listOf(
        BeltReserve(
            idx = 0,
            address = " 0x1AF3F329e8BE154074D8769D1FFa4eE058B1DBc3",
            name = "bdai"
        ),
        BeltReserve(
            idx = 3 ,
            address = "0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56",
            name = "busd"
        ),
        BeltReserve(
            idx = 2,
            address = "0x55d398326f99059fF775485246999027B3197955",
            name = "usdt"
        ),
        BeltReserve(
            idx = 1,
            address = "0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d",
            name = "usdc"
        )
    )

    override fun getExpectedTokens(from: String, to: String, amount: BigInteger): BigInteger {
        val fromReserve = reserves.firstOrNull { it.address.equals(from, ignoreCase = true) }
        val toReserve = reserves.firstOrNull { it.address.equals(to, ignoreCase = true) }
        return if (fromReserve != null && toReserve != null) {
            beltSwapContract.calculateSwap(fromReserve.idx.toBigInteger(), toReserve.idx.toBigInteger(), amount)
        } else {
            BigInteger.ZERO
        }
    }
}

data class BeltReserve(val idx: Long, val address: String, val name: String)