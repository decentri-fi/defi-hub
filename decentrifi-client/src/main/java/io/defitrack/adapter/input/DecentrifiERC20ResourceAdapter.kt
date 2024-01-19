package io.defitrack.adapter.input

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.common.network.Network
import io.defitrack.domain.FungibleToken
import io.defitrack.domain.WrappedToken
import io.defitrack.port.input.ERC20Resource
import io.defitrack.port.output.ERC20s
import io.github.reactivecircus.cache4k.Cache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

@Component
internal class DecentrifiERC20ResourceAdapter(
    private val erC20s: ERC20s
) : ERC20Resource {

    val tokensCache = Cache.Builder<String, List<FungibleToken>>().expireAfterWrite(1.hours).build()

    val tokenCache = Cache.Builder<String, FungibleToken>().expireAfterWrite(1.hours).build()

    val wrappedCache = Cache.Builder<Network, WrappedToken>().build()

    override suspend fun getAllTokens(network: Network, verified: Boolean?): List<FungibleToken> {
        return tokensCache.get("tokens-${network}") {
            erC20s.getAllTokens(network, verified).map {
                it.toFungibleToken()
            }
        }
    }

    override suspend fun getBalance(network: Network, tokenAddress: String, user: String): BigInteger {
        return erC20s.getBalance(network, tokenAddress, user)
    }

    override suspend fun getTokenInformation(network: Network, address: String): FungibleToken {
        return tokenCache.get("token-${network}-${address}") {
            erC20s.getTokenInformation(network, address).toFungibleToken()
        }
    }

    override suspend fun getWrappedToken(network: Network): WrappedToken {
        return wrappedCache.get(network) {
            retry(limitAttempts(3)) {
                erC20s.getWrappedToken(network).toWrappedToken()
            }
        }
    }

    override suspend fun getAllowance(network: Network, token: String, owner: String, spender: String): BigInteger {
        return erC20s.getAllowance(network, token, owner, spender)
    }
}