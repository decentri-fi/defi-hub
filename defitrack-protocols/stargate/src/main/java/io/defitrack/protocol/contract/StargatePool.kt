package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

class StargatePool(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC20Contract(
    blockchainGateway, "", address
) {

    suspend fun token(): String {
        return readSingle(
            "token",
            output = TypeUtils.address()
        )
    }

    suspend fun totalLiquidity(): BigInteger {
        return readSingle(
            "totalLiquidity", output = TypeUtils.uint256()
        )
    }
}