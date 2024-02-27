package io.defitrack.protocol.ethena

import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

context(BlockchainGateway)
class StakedUSDeContract(address: String) : ERC20Contract(address) {

    //contract: 0x9d39a5de30e57443bff2a8307a4256c8797a3497

    suspend fun convertToAssets(amount: BigInteger): BigInteger {
        return readSingle(
            "convertToAssets",
            listOf(amount.toUint256()),
            uint256()
        )
    }

    suspend fun totalAssets(): BigInteger {
        return readSingle(
            "totalAssets",
            uint256()
        )
    }
}