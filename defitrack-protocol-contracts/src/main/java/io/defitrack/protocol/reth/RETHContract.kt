package io.defitrack.protocol.reth

import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

context(BlockchainGateway)
class RETHContract(address: String) : ERC20Contract(address) {

    val exchangeRate = constant<BigInteger>("getExchangeRate", uint256())
    val totalCollateral = constant<BigInteger>("getTotalCollateral", uint256())
}