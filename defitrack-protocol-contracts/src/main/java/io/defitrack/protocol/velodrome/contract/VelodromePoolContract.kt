package io.defitrack.protocol.velodrome.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class VelodromePoolContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {

    val token0 = constant<String>("token0", address())
    val token1 = constant<String>("token1", address())
    val stable = constant<Boolean>("stable", TypeUtils.bool())
    val reserves = constant<Reserves>("getReserves", listOf(
        uint256(), //reserve 0
        uint256(), //reserve 1
        uint256(), //blockTimestampLast
    )) {
        Reserves(
            amount0 = it[0].value as BigInteger,
            amount1 = it[1].value as BigInteger,
        )
    }

    data class Reserves(
        val amount0: BigInteger,
        val amount1: BigInteger,
    )

}