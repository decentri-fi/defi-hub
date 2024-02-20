package io.defitrack.protocol.pendle

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.toUint32
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigDecimal
import java.math.BigInteger

class PendleOracleContract(blockchainGateway: BlockchainGateway, address: String) : EvmContract(
    blockchainGateway, address
) {

    suspend fun getPtToAssetRate(market: String, duration: Int = 1): BigDecimal {
        val retVal = read(
            "getPtToAssetRate",
            listOf(market.toAddress(), duration.toUint32()),
            uint256().nel()
        )

        return (retVal[0].value as BigInteger).asEth()
    }
}