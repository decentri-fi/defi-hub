package io.defitrack.market.position

import io.defitrack.evm.multicall.MultiCallElement
import org.web3j.abi.datatypes.Type
import java.math.BigInteger

class PositionFetcher(
    val address: String,
    val function: (user: String) -> org.web3j.abi.datatypes.Function,
    val extractBalance: suspend (List<Type<*>>) -> Position = { result ->
        try {
            val result = result[0].value as BigInteger
            Position(result, result)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Position.ZERO
        }
    }
) {
    fun toMulticall(user: String): MultiCallElement {
        return MultiCallElement(
            function(user), address
        )
    }
}