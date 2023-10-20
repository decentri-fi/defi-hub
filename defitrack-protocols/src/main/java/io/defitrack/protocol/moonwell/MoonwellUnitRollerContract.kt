package io.defitrack.protocol.moonwell

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.protocol.compound.v2.contract.CompoundComptrollerContract
import org.web3j.abi.datatypes.Function

class MoonwellUnitRollerContract(
    blockchainGateway: BlockchainGateway, address: String
) : CompoundComptrollerContract(blockchainGateway, address) {


    fun claimReward(user: String): ContractCall {
        return createFunction(
            "claimReward",
            inputs = listOf(
                user.toAddress()
            ),
            outputs = emptyList()
        ).toContractCall()
    }

    val rewardDistributor = constant<String>("rewardDistributor", TypeUtils.address())
}