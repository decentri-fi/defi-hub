package io.defitrack.protocol.gmx

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function

class StakedGMXContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(blockchainGateway, address) {

    fun claimableFn(address: String): ContractCall {
        return createFunction(
            "claimable",
            listOf(address.toAddress()),
            listOf(uint256())
        )
    }

    fun claimFn(address: String): ContractCall {
        return createFunction("claim", listOf(address.toAddress()))
    }

    val rewardToken = lazyAsync {
        readSingle<String>("rewardToken", address())
    }
}