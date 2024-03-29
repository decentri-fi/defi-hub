package io.defitrack.protocol.stader

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class MaticXStakingContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(blockchainGateway, address) {

    suspend fun convertMaticXToMatic(amount: BigInteger) : BigInteger {
        return readSingle(
            "convertMaticXToMatic",
            amount.toUint256().nel(),
            uint256()
        )
    }
}