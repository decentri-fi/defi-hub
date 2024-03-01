package io.defitrack.protocol.pendle

import arrow.core.Option
import arrow.core.getOrElse
import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.uint8
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

context(BlockchainGateway)
class PendleSyContract(address: String) : EvmContract(address) {
    suspend fun asset(): String {
        return Option.catch {
            readSingle<String>("asset", TypeUtils.address())
        }.getOrElse {
            val result = read("assetInfo", emptyList(), listOf(uint8(), TypeUtils.address(), uint8()))
            return@getOrElse result[1].value as String
        }
    }

    suspend fun yieldToken(): String {
        return readSingle("yieldToken", TypeUtils.address())
    }
}