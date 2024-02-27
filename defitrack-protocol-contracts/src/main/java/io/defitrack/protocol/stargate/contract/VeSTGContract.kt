package io.defitrack.protocol.stargate.contract

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract

class VeSTGContract(
    blockchainGateway: BlockchainGateway,
    address: String,
) : DeprecatedEvmContract(blockchainGateway, address) {


    fun lockedFn(user: String): ContractCall {
        return createFunction(
            "locked",
            user.toAddress().nel(),
            listOf(
                uint128(),
                uint256()
            )
        )
    }
}