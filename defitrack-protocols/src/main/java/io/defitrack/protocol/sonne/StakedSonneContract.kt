package io.defitrack.protocol.sonne

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class StakedSonneContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {

    val sonne = constant<String>("sonne", TypeUtils.address())

    fun getClaimableFn(token: String, account: String): Function {
        return createFunction(
            "getClaimable",
            listOf(
                token.toAddress(),
                account.toAddress()
            ),
            listOf(TypeUtils.uint256())
        )
    }

    fun claimAllFn(): ContractCall {
        return createFunction(
            "claimAll",
        ).toContractCall()
    }

    suspend fun tokens(): List<String> {
        return readMultiCall(
            (0 until 10).map {
                createFunction(
                    "tokens",
                    listOf(it.toBigInteger().toUint256()),
                    listOf(TypeUtils.address())
                )
            }
        ).filter {
            it.success
        }.map { it.data[0].value as String }
            .filter {
                it != "0x0000000000000000000000000000000000000000"
            }
    }
}