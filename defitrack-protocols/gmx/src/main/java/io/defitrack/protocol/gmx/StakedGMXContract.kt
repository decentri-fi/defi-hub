package io.defitrack.protocol.gmx

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.web3j.abi.datatypes.Function

class StakedGMXContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(blockchainGateway, "", address) {

    fun claimableFn(address: String): Function {
        return createFunction(
            "claimable",
            listOf(address.toAddress()),
            listOf(uint256())
        )
    }

    fun claimFn(address: String): Function {
        return createFunction("claim", listOf(address.toAddress()))
    }

    val rewardToken = GlobalScope.async<String>(start = CoroutineStart.LAZY) {
        readSingle("rewardToken", TypeUtils.address())
    }
}