package io.defitrack.protocol.aura

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint112
import io.defitrack.abi.TypeUtils.Companion.uint32
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract

class VoteLockedAuraContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    fun balances(user: String): ContractCall {
        return createFunction(
            "balances",
            listOf(user.toAddress()),
            listOf(uint112(), uint32())
        )
    }

    suspend fun rewardTokens(): List<String> {
        return readMultiCall(
            (0 until 3).map { index ->
                createFunction(
                    "rewardTokens",
                    listOf(index.toUint256()),
                    listOf(TypeUtils.address())
                )
            }
        ).filter { it.success }
            .map { it.data[0].value as String }
    }
}