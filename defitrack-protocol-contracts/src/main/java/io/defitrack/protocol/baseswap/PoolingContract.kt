package io.defitrack.protocol.baseswap

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.uint112
import io.defitrack.abi.TypeUtils.Companion.uint32
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class PoolingContract(address: String) : EvmContract(address) {

    val token0 = constant<String>("token0", TypeUtils.address())
    val token1 = constant<String>("token1", TypeUtils.address())

    suspend fun getReserves(): Reserves {
        val result = read(
            "getReserves",
            listOf(),
            listOf(
                uint112(),
                uint112(),
                uint32(),
            )
        )

        return Reserves(
            result[0].value as BigInteger,
            result[1].value as BigInteger
        )
    }


}

data class Reserves(
    val reserve0: BigInteger,
    val reserve1: BigInteger
)
