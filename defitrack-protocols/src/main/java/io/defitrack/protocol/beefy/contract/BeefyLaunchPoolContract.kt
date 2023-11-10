package io.defitrack.protocol.beefy.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

class BeefyLaunchPoolContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {

    val stakedToken = constant<String>("stakedToken", TypeUtils.address())

}