package io.defitrack.protocol.bancor.contract

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class BancorNetworkContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, address
) {

    fun depositFunction(token: String, amount: BigInteger): Function {
        return createFunction(
            "deposit",
            listOf(
                token.toAddress(),
                amount.toUint256()
            )
        )
    }
}