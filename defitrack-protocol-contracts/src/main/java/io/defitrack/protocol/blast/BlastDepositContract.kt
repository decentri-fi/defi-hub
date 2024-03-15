package io.defitrack.protocol.blast

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract

class BlastDepositContract(
    blockchainGateway: BlockchainGateway, address: String
): DeprecatedEvmContract(
    blockchainGateway, address
) {

    fun usdShares(user: String): ContractCall {
        return createFunction(
            "usdShares",
            user.toAddress().nel(),
            uint256().nel()
        )
    }

    fun ethShares(user: String): ContractCall {
        return createFunction(
            "ethShares",
            user.toAddress().nel(),
            uint256().nel()
        )
    }
}