package io.defitrack.protocol.eigenlayer

import arrow.core.nel
import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class StrategyContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    val totalShares = constant<BigInteger>("totalShares", uint256())
    val underlyingToken = constant<String>("underlyingToken", TypeUtils.address())

    fun userUnderlyingView(user: String): ContractCall {
        return createFunction("userUnderlyingView", listOf(user.toAddress()), uint256().nel())
    }

    suspend fun sharesToUnderlying(shares: BigInteger) {
        return readSingle("sharesToUnderlying", listOf(shares.toUint256()), uint256())
    }

    fun sharesFn(user: String): ContractCall {
        return createFunction(
            "shares",
            listOf(user.toAddress()),
        )
    }
}