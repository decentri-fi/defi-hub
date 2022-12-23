package io.defitrack.protocol.aave.staking

import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class StakedAaveContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway,
    "", address
) {

    suspend fun totalSupply(): BigInteger {
        return readWithoutAbi(
            "totalSupply",
            emptyList(),
            listOf(uint256())
        )[0].value as BigInteger
    }
}