package io.defitrack.protocol

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Type

class OlympusStakingContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(blockchainGateway, "", address) {

   suspend fun gOHM(): String {
        return readWithoutAbi("gOHM", outputs = listOf(TypeUtils.address()))[0].value as String
    }
}