package io.defitrack.protocol.algebra

import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class AlgebraPoolContract(
    blockchainGateway: BlockchainGateway, abi: String
) : EvmContract(
    blockchainGateway, abi
) {

    val liquidity = constant<BigInteger>("liquidity", uint128())
}