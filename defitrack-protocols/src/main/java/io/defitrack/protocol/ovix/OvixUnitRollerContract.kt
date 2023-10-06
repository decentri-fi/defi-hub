package io.defitrack.protocol.ovix

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.protocol.compound.v2.contract.CompoundComptrollerContract
import org.web3j.abi.datatypes.Function

class OvixUnitRollerContract(
    blockchainGateway: BlockchainGateway, address: String
) : CompoundComptrollerContract(blockchainGateway, address) {

    fun claimReward(user: String): Function {
        return createFunction(
            "claimReward",
            inputs = listOf(
                user.toAddress()
            ),
            outputs = emptyList()
        )
    }
}