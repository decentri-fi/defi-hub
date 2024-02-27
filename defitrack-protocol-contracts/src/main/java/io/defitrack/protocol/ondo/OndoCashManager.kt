package io.defitrack.protocol.ondo

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class OndoCashManager(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {
    val cash = constant<String>("cash", address())
    val collateral = constant<String>("collateral", address())
    val lastSetMintExchangeRate = constant<BigInteger>("lastSetMintExchangeRate", uint256())
}