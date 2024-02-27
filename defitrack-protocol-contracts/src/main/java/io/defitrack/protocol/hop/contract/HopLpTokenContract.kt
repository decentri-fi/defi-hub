package io.defitrack.protocol.hop.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toUint8
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

context(BlockchainGateway)
class HopLpTokenContract(address: String) : ERC20Contract(address) {

    suspend fun swap(): String {
        return readSingle("swap", address())
    }

    suspend fun getToken(index: Int): String {
        return read(
            "getToken",
            listOf(index.toBigInteger().toUint8()),
            listOf(address())
        )[0].value as String
    }
}