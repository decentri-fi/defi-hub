package io.defitrack.protocol.moonwell

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.protocol.compound.v2.contract.CompoundComptrollerContract

context(BlockchainGateway)
class MoonwellUnitRollerContract(address: String
) : CompoundComptrollerContract(address) {


    fun claimReward(user: String): ContractCall {
        return createFunction(
            "claimReward",
            inputs = listOf(
                user.toAddress()
            )
        )
    }

    val rewardDistributor = constant<String>("rewardDistributor", address())
}