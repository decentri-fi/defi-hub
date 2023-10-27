package io.defitrack.protocol.olympusdao

import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

class GOHMContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC20Contract(blockchainGateway, address) {

    suspend fun balanceFrom(from: BigInteger): BigInteger {
        return read(
            "balanceFrom",
            inputs = listOf(from.toUint256()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }
}
