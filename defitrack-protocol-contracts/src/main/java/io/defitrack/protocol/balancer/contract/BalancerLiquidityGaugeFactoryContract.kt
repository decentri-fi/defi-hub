package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

context(BlockchainGateway)
class BalancerLiquidityGaugeFactoryContract(address: String) : EvmContract(address) {
    suspend fun getPoolGauge(poolAddress: String): String {
        return read(
            "getPoolGauge",
            listOf(poolAddress.toAddress()),
            listOf(TypeUtils.address())
        )[0].value as String
    }
}