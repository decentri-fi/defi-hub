package io.defitrack.protocol.radiant

import io.defitrack.abi.TypeUtils.Companion.dynamicArray
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.StaticStruct
import org.web3j.abi.datatypes.generated.Uint256

class RadiantMultiFeeDistributor(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {


    fun getClaimableRewardFn(user: String): Function {
        return createFunction(
            "claimableRewards",
            listOf(user.toAddress()),
            listOf(dynamicArray<Reward>())
        )
    }

    data class Reward(
        val rewardAddress: Address,
        val amount: Uint256
    ) : StaticStruct(rewardAddress, amount)

}