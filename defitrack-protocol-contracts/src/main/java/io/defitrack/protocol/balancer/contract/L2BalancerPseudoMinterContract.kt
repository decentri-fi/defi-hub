package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class L2BalancerPseudoMinterContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    suspend fun minted(user: String, gauge: String): BigInteger {
        return readSingle(
            "minted",
            listOf(user.toAddress(), gauge.toAddress()),
            uint256()
        )
    }

    fun mint(amount: BigInteger, user: String): ContractCall {
        return createFunction(
            "mint",
            listOf(user.toAddress(), amount.toUint256()),
            listOf()
        )
    }
}