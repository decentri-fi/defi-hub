package io.defitrack.protocol.moonwell

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.protocol.compound.v2.contract.CompoundComptrollerContract

class MoonwellUnitRollerContract(
    blockchainGateway: BlockchainGateway, address: String
) : CompoundComptrollerContract(blockchainGateway, address) {


    fun claimReward(user: String): MutableFunction {
        return createFunction(
            "claimReward",
            inputs = listOf(
                user.toAddress()
            ),
            outputs = emptyList()
        ).toMutableFunction()
    }

    val rewardDistributor = constant<String>("rewardDistributor", TypeUtils.address())
}