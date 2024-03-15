package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract

class BalancerLiquidityGaugeFactoryContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    suspend fun getPoolGauge(poolAddress: String): String {
        return read(
            "getPoolGauge",
            listOf(poolAddress.toAddress()),
            listOf(TypeUtils.address())
        )[0].value as String
    }

}