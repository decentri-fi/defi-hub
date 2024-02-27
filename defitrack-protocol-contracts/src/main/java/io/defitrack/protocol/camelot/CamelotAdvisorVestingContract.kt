package io.defitrack.protocol.camelot

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class CamelotAdvisorVestingContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(blockchainGateway, address) {

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