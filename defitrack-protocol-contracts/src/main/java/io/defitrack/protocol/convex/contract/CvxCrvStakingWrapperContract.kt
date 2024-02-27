package io.defitrack.protocol.convex.contract

import arrow.core.nel
import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import kotlinx.coroutines.Deferred

context(BlockchainGateway)
class CvxCrvStakingWrapperContract( address: String) : ERC20Contract(address) {

    //todo: fix
    val crv: Deferred<String> = lazyAsync {
        readSingle("crv", TypeUtils.address())
    }

    fun getRewardFn(user: String): ContractCall {
        return createFunction(
            "getReward",
            user.toAddress().nel(),
        )
    }

    val cvxCrv: Deferred<String> = lazyAsync {
        readSingle("cvxCrv", TypeUtils.address())
    }
}