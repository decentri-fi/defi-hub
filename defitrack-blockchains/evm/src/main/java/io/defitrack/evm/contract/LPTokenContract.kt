package io.defitrack.evm.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import kotlinx.coroutines.Deferred
import java.math.BigDecimal
import java.math.BigInteger

context(BlockchainGateway)
class LPTokenContract(address: String) : ERC20Contract(address) {

    val token0: Deferred<String> = constant("token0", address())
    val token1: Deferred<String> = constant("token1", address())

    val totalSupply: Deferred<BigInteger> = constant("totalSupply", uint256())
    val decimals: Deferred<BigInteger> = constant("decimals", uint256())

    override suspend fun totalDecimalSupply(): Refreshable<BigDecimal> {
        return refreshable(totalSupply.await().asEth(decimals.await())) {
            try {
                readSingle("totalSupply", uint256())
            } catch (ex: Exception) {
                BigInteger.ZERO
            }.asEth(decimals.await())
        }
    }
}