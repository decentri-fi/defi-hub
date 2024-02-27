package io.defitrack.protocol.lido

import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class StakedMaticContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
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