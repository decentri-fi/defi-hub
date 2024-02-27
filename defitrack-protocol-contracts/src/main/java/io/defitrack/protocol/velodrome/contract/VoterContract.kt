package io.defitrack.protocol.velodrome.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract


class VoterContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    suspend fun gauges(poolAddress: String): String {
        return read(
            "gauges",
            inputs = listOf(poolAddress.toAddress()),
            outputs = listOf(address())
        )[0].value as String
    }
}