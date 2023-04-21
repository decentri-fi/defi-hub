package io.defitrack.protocol.algebra

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class AlgebraPoolContract(
    blockchainGateway: BlockchainGateway, abi: String
) : EvmContract(
    blockchainGateway, "", abi
) {

    suspend fun liquidity(): BigInteger {
        return readSingle("liquidity", uint128())
    }

    suspend fun token0() : String {
        return readSingle("token0", address())
    }

    suspend fun token1() : String {
        return readSingle("token1", address())
    }
}