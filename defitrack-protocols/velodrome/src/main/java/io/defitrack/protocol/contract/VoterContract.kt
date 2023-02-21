package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract


class VoterContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    suspend fun gauges(poolAddress: String): String {
        return readWithoutAbi(
            "gauges",
            inputs = listOf(poolAddress.toAddress()),
            outputs = listOf(address())
        )[0].value as String
    }
}