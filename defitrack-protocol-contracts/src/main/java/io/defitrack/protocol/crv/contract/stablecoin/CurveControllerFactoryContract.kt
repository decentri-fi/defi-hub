package io.defitrack.protocol.crv.contract.stablecoin

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class CurveControllerFactoryContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    val nCollaterals = constant<BigInteger>("n_collaterals", uint256())

    suspend fun controllers(): List<String> {
        return readMultiCall(
            (0 until nCollaterals.await().toInt()).map {
                createFunction(
                    "controllers",
                    it.toBigInteger().toUint256().nel(),
                    address().nel()
                )
            }
        ).filter {
            it.success
        }.map { it.data.first().value as String }
    }

}