package io.defitrack.staking.domain

import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.datatypes.Type
import java.math.BigInteger

class StakingMarketBalanceFetcher(
    val address: String,
    val function: (user: String) -> org.web3j.abi.datatypes.Function,
    val extractBalance: (List<Type<*>>) -> BigInteger = { result ->
        result[0].value as BigInteger
    }
) {
    fun toMulticall(user: String): MultiCallElement {
        return MultiCallElement(
            function(user), address
        )
    }
}