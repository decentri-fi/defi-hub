package io.defitrack.evm.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import kotlinx.coroutines.Deferred

class LPTokenContract(
    solidityBasedContractAccessor: BlockchainGateway,
    address: String
) : ERC20Contract(solidityBasedContractAccessor, address) {

    val token0: Deferred<String> = constant("token0", TypeUtils.address())
    val token1: Deferred<String> = constant("token1", TypeUtils.address())
}