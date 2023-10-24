package io.defitrack.protocol.mycelium

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function

class TcrStakingRewards(blockchainGateway: BlockchainGateway, address: String) : EvmContract(
    blockchainGateway, address
) {

    val rewardsToken = constant<String>("rewardsToken", TypeUtils.address())
    val stakingToken = constant<String>("stakingToken", TypeUtils.address())

    fun exitFn(): ContractCall {
        return createFunction(
            "exit",
            emptyList(),
            emptyList()
        ).toContractCall()
    }
}