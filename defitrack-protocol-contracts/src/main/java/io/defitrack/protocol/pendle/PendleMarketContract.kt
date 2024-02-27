package io.defitrack.protocol.pendle

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract

context(BlockchainGateway)
class PendleMarketContract(
    address: String
) : ERC20Contract(
    address
) {

    fun activeBalanceFn(user: String): ContractCall {
        return createFunction(
            "activeBalance",
            user.toAddress().nel(),
            uint256().nel()
        )
    }

    suspend fun readTokens(): PendleMarket {
        val retVal = read(
            "readTokens",
            emptyList(),
            listOf(
                address(),
                address(),
                address(),
            )
        )

        return PendleMarket(
            sy = retVal[0].toString(),
            pt = retVal[1].toString(),
            yt = retVal[2].toString()
        )
    }

    data class PendleMarket(
        val sy: String,
        val pt: String,
        val yt: String
    )

}