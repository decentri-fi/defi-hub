package io.defitrack.token

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.token.domain.ERC20Information
import io.defitrack.common.network.Network
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class ERC20Resource(private val client: HttpClient) {

    fun getBalance(network: Network, tokenAddress: String, user: String): BigInteger {
        return runBlocking {
            client.get("https://api.defitrack.io/erc20/${network.name}/$tokenAddress/$user")
        }
    }

    fun getERC20(network: Network, address: String): ERC20Information {
        return runBlocking {
            retry(limitAttempts(3)) { client.get("https://api.defitrack.io/erc20/${network.name}/$address") }
        }
    }
}