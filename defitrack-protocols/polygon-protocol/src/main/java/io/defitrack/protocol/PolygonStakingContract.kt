package io.defitrack.protocol

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class PolygonStakingContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    fun totalStakedForFn(user: String): Function {
        return createFunction(
            "totalStakedFor",
            inputs = listOf(user.toAddress()),
            outputs = listOf(uint256())
        )
    }

    suspend fun token() : String {
        return readSingle("token", address())
    }
}