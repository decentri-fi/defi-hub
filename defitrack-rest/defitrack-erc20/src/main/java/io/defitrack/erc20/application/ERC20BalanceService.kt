package io.defitrack.erc20.application

import arrow.core.Either.Companion.catch
import arrow.core.getOrElse
import io.defitrack.common.network.Network
import io.defitrack.erc20.port.output.ReadBalancePort
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger

@Component
class ERC20BalanceService(
    private val readBalancePort: ReadBalancePort
) {
    suspend fun getBalance(
        network: Network,
        tokenAddress: String,
        userAddress: String
    ): BigInteger {
        return catch {
            if (tokenAddress == "0x0") {
                readBalancePort.getNativeBalance(network, userAddress).times(BigDecimal.TEN.pow(18)).toBigInteger()
            } else {
                readBalancePort.getBalance(network, tokenAddress, userAddress)
            }
        }.mapLeft {
            it.printStackTrace()
        }.getOrElse {
            BigInteger.ZERO
        }
    }
}