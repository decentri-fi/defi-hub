package io.defitrack.erc20.application

import arrow.core.Either.Companion.catch
import arrow.core.getOrElse
import io.defitrack.common.network.Network
import io.defitrack.erc20.port.input.AllowanceUseCase
import io.defitrack.erc20.port.output.ReadAllowancePort
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class ERC20AllowanceService(
    private val readAllowancePort: ReadAllowancePort
) : AllowanceUseCase {
    override suspend fun getAllowance(
        network: Network,
        owner: String,
        spender: String,
        tokenAddress: String
    ): BigInteger {
        return catch {
            readAllowancePort.getAllowance(network, owner, spender, tokenAddress)
        }.mapLeft {
            it.printStackTrace()
        }.getOrElse {
            BigInteger.ZERO
        }
    }
}