package io.defitrack.erc20

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.common.network.Network
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.erc20.domain.WrappedToken
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.erc20.port.out.ERC20s
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

@Component
internal class DecentrifiERC20ResourceAdapter(
    private val erC20s: ERC20s
) : ERC20Resource {

    val tokensCache = Cache.Builder<String, List<FungibleTokenInformation>>().expireAfterWrite(1.hours).build()

    val tokenCache = Cache.Builder<String, FungibleTokenInformation>().expireAfterWrite(1.hours).build()

    val wrappedCache = Cache.Builder<Network, WrappedToken>().build()

    override suspend fun getAllTokens(network: Network, verified: Boolean?): List<FungibleTokenInformation> {
        return tokensCache.get("tokens-${network}-$verified") {
            erC20s.getAllTokens(network, verified)
        }
    }

    override suspend fun getBalance(network: Network, tokenAddress: String, user: String): BigInteger {
        return erC20s.getBalance(network, tokenAddress, user)
    }

    override suspend fun getTokenInformation(network: Network, address: String): FungibleTokenInformation {
        return tokenCache.get("token-${network}-${address}") {
            erC20s.getTokenInformation(network, address)
        }
    }

    override suspend fun getWrappedToken(network: Network): WrappedToken {
        return wrappedCache.get(network) {
            retry(limitAttempts(3)) {
                erC20s.getWrappedToken(network)
            }
        }
    }

    override suspend fun getAllowance(network: Network, token: String, owner: String, spender: String): BigInteger {
        return erC20s.getAllowance(network, token, owner, spender)
    }
}