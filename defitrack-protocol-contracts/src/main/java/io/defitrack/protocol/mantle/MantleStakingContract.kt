package io.defitrack.protocol.mantle

import arrow.core.Either
import arrow.core.getOrElse
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class MantleStakingContract(
    address: String
) : EvmContract(address) {

    suspend fun mEThToEth(amount: BigInteger): BigInteger {
        return Either.catch<BigInteger> {
            readSingle("mETHToETH", listOf(amount.toUint256()), uint256())
        }.mapLeft {
            logger.error("unable to fetch mETHToETH")
        }.getOrElse { BigInteger.ZERO }
    }
}