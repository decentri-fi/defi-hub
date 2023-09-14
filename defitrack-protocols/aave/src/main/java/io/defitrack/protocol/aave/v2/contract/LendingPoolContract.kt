package io.defitrack.protocol.aave.v2.contract

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint16
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class LendingPoolContract(blockchainGateway: BlockchainGateway, address: String) :
    EvmContract(
        blockchainGateway, address
    ) {

    fun depositFunction(asset: String, amount: BigInteger): Function {
        return createFunction(
            "deposit",
            listOf(
                asset.toAddress(),
                amount.toUint256(),
                "0000000000000000000000000000000000000000".toAddress(),
                BigInteger.ZERO.toUint16()
            ),
            emptyList()
        )
    }
}