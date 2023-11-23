package io.defitrack.market.position

import io.defitrack.evm.contract.ContractCall
import org.web3j.abi.datatypes.Type
import java.math.BigInteger

class PositionFetcher(
    val functionCreator: (user: String) -> ContractCall,
    val extractBalance: suspend (List<Type<*>>) -> Position = defaultExtraction()
) {

    companion object {
        fun defaultExtraction(): suspend (List<Type<*>>) -> Position = { result ->
            try {
                val result = result[0].value as BigInteger
                Position(result, result)
            } catch (ex: Exception) {
                ex.printStackTrace()
                Position.ZERO
            }
        }
    }
}