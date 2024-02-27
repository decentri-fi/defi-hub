package io.defitrack.protocol.puffer

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger
import java.util.logging.Logger

class PufferEthContract(
    blockchainGateway: BlockchainGateway, address: String,
) : ERC20Contract(
    blockchainGateway, address
) {

    //TODO: create a provider for this

    suspend fun convertToAssets(shares: BigInteger): BigInteger {
        return readSingle(
            "convertToAssets",
            shares.toUint256().nel(),
            uint256()
        )
    }

    suspend fun totalAssets() : BigInteger {
        return readSingle(
            "totalAssets",
            emptyList(),
            uint256()
        )
    }
}