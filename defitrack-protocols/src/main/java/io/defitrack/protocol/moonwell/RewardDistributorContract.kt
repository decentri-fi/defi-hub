package io.defitrack.protocol.moonwell

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256

class RewardDistributorContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {

    fun getOutstandingRewardsForUserFn(mToken: String): (String) -> ContractCall {
        return { user ->
            createFunction(
                "getOutstandingRewardsForUser",
                inputs = listOf(
                    mToken.toAddress(),
                    user.toAddress()
                ),
                outputs = listOf(
                    object : TypeReference<DynamicArray<Reward>>() {

                    }
                )
            )
        }
    }

    data class Reward(
        val emissionToken: Address,
        val amount: Uint256,
        val supplySide: Uint256,
        val borrowSide: Uint256,
    ) : StaticStruct(
        emissionToken, amount, supplySide, borrowSide
    )
}