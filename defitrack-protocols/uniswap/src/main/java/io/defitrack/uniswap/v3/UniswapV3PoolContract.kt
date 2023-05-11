package io.defitrack.uniswap.v3

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class UniswapV3PoolContract(
    blockchaingateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchaingateway, "", address
) {

    suspend fun slot0(): Slot0 {
        val retVal = readWithoutAbi(
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

    data class Slot0(
        val tick: BigInteger
    )
}