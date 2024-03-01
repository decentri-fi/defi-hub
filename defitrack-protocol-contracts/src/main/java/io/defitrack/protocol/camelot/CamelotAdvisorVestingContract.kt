package io.defitrack.protocol.camelot

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class CamelotAdvisorVestingContract(
    address: String
) : EvmContract(address) {

    fun releaseFn(): ContractCall {
        return createFunction("release")
    }

    suspend fun beneficiariesShares(address: String): BigInteger {
        return read(
            "beneficiariesShare",
            listOf(address.toAddress()),
            listOf(uint256())
        )[0].value as BigInteger
    }

    val totalShare = constant<BigInteger>("totalShare", uint256())
    val xGrailToken = constant<String>("xgrailToken", TypeUtils.address())
    fun releasableFunction(): (String) -> ContractCall {
        return {
            createFunction(
                "releasable",
                emptyList(),
                listOf(uint256())
            )
        }
    }
}