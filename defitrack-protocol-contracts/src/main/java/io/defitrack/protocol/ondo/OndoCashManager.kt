package io.defitrack.protocol.ondo

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class OndoCashManager(
    address: String
) : EvmContract(
    address
) {
    val cash = constant<String>("cash", address())
    val collateral = constant<String>("collateral", address())
    val lastSetMintExchangeRate = constant<BigInteger>("lastSetMintExchangeRate", uint256())
}