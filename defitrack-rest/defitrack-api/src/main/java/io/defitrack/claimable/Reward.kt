package io.defitrack.claimable

import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.token.FungibleToken
import org.web3j.abi.datatypes.Type
import java.math.BigInteger

data class Reward(
    val token: FungibleToken,
    val contractAddress: String,
    val getRewardFunction: suspend (String) -> org.web3j.abi.datatypes.Function,
    val extractAmountFromRewardFunction: suspend (List<Type<*>>, String) -> BigInteger = { result, user ->
        result[0].value as BigInteger
    },
) {
    suspend fun toMulticall(user: String): MultiCallElement {
        return MultiCallElement(
            getRewardFunction(user), contractAddress
        )
    }
}