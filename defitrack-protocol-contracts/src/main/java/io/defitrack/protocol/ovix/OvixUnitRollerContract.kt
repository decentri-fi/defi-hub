package io.defitrack.protocol.ovix

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.protocol.compound.v2.contract.CompoundComptrollerContract

class OvixUnitRollerContract(
    blockchainGateway: BlockchainGateway, address: String
) : CompoundComptrollerContract(blockchainGateway, address) {

    fun claimReward(user: String): ContractCall {
        return createFunction(
            "claimReward",
            inputs = listOf(user.toAddress())
        )
    }
}