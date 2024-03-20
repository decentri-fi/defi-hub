package io.defitrack.protocol.stargate.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

context(BlockchainGateway)
class StargatePool(
    address: String
) : ERC20Contract(address) {

    val token = constant<String>("token", address())
    val totalLiquidity = constant<BigInteger>("totalLiquidity", uint256())
}