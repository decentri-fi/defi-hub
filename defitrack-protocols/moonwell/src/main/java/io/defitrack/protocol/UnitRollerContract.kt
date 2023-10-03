package io.defitrack.protocol

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function

class UnitRollerContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {

    suspend fun getMarkets(): List<String> {
        return (read(
            "getAllMarkets",
            inputs = emptyList(),
            outputs = listOf(TypeUtils.dynamicArray<Address>())
        )[0].value as List<Address>).map {
            it.value as String
        }
    }

    fun claimReward(user: String): Function {
        return createFunction(
            "claimReward",
            inputs = listOf(
                user.toAddress()
            ),
            outputs = emptyList()
        )
    }

    val rewardDistributor = constant<String>("rewardDistributor", TypeUtils.address())
}