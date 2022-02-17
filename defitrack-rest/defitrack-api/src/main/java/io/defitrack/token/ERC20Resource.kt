package io.defitrack.token

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereumbased.contract.ERC20Contract
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import io.defitrack.ethereumbased.contract.multicall.MultiCallElement
import io.defitrack.token.domain.ERC20Information
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class ERC20Resource(
    private val client: HttpClient,
    private val abiResource: ABIResource
) {

    val erc20ABI by lazy {
        abiResource.getABI("general/ERC20.json")
    }

    fun getAllTokens(network: Network): List<ERC20Information> {
        return runBlocking(Dispatchers.IO) {
            retry(limitAttempts(3)) { client.get("https://api.defitrack.io/erc20/${network.name}") }
        }
    }

    fun getBalance(network: Network, tokenAddress: String, user: String): BigInteger {
        return runBlocking(Dispatchers.IO) {
            client.get("https://api.defitrack.io/erc20/${network.name}/$tokenAddress/$user")
        }
    }

    fun getERC20(network: Network, address: String): ERC20Information {
        return runBlocking(Dispatchers.IO) {
            retry(limitAttempts(3)) { client.get("https://api.defitrack.io/erc20/${network.name}/$address") }
        }
    }

    fun getTokenInformation(network: Network, address: String): Token {
        return runBlocking(Dispatchers.IO) {
            retry(limitAttempts(3)) { client.get("https://api.defitrack.io/erc20/${network.name}/$address/token") }
        }
    }

    fun getBalancesFor(
        address: String,
        tokens: List<String>,
        evmContractAccessor: EvmContractAccessor
    ): List<BigInteger> {
        return evmContractAccessor.readMultiCall(tokens.map {
            MultiCallElement(
                ERC20Contract(
                    evmContractAccessor,
                    erc20ABI,
                    it
                ).balanceOfMethod(address),
                it
            )
        }
        ).map {
            try {
                it[0].value as BigInteger
            } catch (_: Exception) {
                BigInteger.ZERO
            }
        }
    }
}