package io.defitrack.claimable

import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.datatypes.Type
import java.math.BigInteger

class ClaimableRewardFetcher(
    val address: String,
    val function: suspend (user: String) -> org.web3j.abi.datatypes.Function,
    val extract: (List<Type<*>>) -> BigInteger = { result ->
        result[0].value as BigInteger
    }
) {
    suspend fun toMulticall(user: String): MultiCallElement {
        return MultiCallElement(
            function(user), address
        )
    }
}