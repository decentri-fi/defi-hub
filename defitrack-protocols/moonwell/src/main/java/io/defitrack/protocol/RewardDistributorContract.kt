package io.defitrack.protocol

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.Array
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.StaticArray1
import org.web3j.abi.datatypes.generated.StaticArray2
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.tuples.generated.Tuple1

class RewardDistributorContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {

    fun getOutstandingRewardsForUserFn(mToken: String, address: String): Function {
        return createFunction(
            "getOutstandingRewardsForUser",
            inputs = listOf(
                mToken.toAddress(),
                address.toAddress()
            ),
            outputs = listOf(
                object : TypeReference<DynamicArray<Reward>>() {

                }
            )
        )
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