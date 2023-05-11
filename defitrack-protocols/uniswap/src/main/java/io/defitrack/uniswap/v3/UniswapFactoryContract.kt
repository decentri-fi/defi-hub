package io.defitrack.uniswap.v3

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint24
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class UniswapFactoryContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, "", address
) {
    suspend fun getPool(
        token0: String,
        token1: String,
        fee: BigInteger
    ): String {
        return readWithoutAbi(
            "getPool",
            listOf(
                token0.toAddress(),
                token1.toAddress(),
                fee.toUint24()
            ),
            listOf(address())
        )[0].value as String
    }
}

