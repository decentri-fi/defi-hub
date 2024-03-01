package io.defitrack.protocol.crv.contract

import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import kotlinx.coroutines.Deferred
import java.math.BigInteger

context(BlockchainGateway)
class CurvePoolContract( address: String) : EvmContract( address
) {
    val virtualPrice: Deferred<BigInteger> = constant("get_virtual_price", uint256())
}