package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

class StargatePool(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC20Contract(
    blockchainGateway, address
) {

    suspend fun token(): String {
        return readSingle("token", address())
    }

    suspend fun totalLiquidity(): BigInteger {
        return readSingle("totalLiquidity", output = uint256())
    }
}