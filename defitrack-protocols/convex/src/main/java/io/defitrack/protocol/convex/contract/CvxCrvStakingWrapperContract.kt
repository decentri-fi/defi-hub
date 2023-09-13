package io.defitrack.protocol.convex.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import kotlinx.coroutines.Deferred

class CvxCrvStakingWrapperContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(blockchainGateway, "", address) {

    val crv: Deferred<String> = lazyAsync {
        readSingle("crv", TypeUtils.address())
    }

    val cvx: Deferred<String> = lazyAsync {
        readSingle("cvx", TypeUtils.address())
    }

    val cvxCrv: Deferred<String> = lazyAsync {
        readSingle("cvxCrv", TypeUtils.address())
    }
}