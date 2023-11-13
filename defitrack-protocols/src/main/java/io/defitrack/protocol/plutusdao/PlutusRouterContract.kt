package io.defitrack.protocol.plutusdao

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function

class PlutusRouterContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {


    fun claimEsPls(): ContractCall {
        return createFunction(
            "claimEsPls",
            emptyList(),
            emptyList()
        ).toContractCall()
    }
}