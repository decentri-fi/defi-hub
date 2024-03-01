package io.defitrack.protocol.lido

import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class StakedMaticContract(
    address: String
) : EvmContract(
    address
) {

    //todo: uses balanceof
    fun convertToStMaticFunction(amount: BigInteger): ContractCall {
        return createFunction(
            "convertToStMaticToMatic",
            inputs = listOf(amount.toUint256()),
            outputs = listOf(uint256())
        )
    }
}