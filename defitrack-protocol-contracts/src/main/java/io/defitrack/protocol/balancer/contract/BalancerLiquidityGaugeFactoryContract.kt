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

    suspend fun getPoolGauges(poolAddresses: List<String>): List<String> {
       return readMultiCall(
            poolAddresses.map {
                createFunction(
                    "getPoolGauge",
                    listOf(it.toAddress()),
                    listOf(TypeUtils.address())
                )
            }
        ).map {
            if (it.success) {
                it.data[0].value as String
            } else {
                "0x0000000000000000000000000000000000000000"
            }
        }
    }

}