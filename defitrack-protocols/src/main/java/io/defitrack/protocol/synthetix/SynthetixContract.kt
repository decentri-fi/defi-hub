package io.defitrack.protocol.synthetix

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class SynthetixContract(blockchainGateway: BlockchainGateway, address: String) : EvmContract(
    blockchainGateway, address
) {

    fun collateralFn(user: String): ContractCall {
        return createFunction(
            "collateral",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }
}