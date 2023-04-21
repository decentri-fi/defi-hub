package io.defitrack.protocol.camelot

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
}