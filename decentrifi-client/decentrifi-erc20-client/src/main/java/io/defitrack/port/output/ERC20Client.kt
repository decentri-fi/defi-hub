package io.defitrack.port.output

import io.defitrack.adapter.output.ERC20RestClient
import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.erc20.WrappedTokenDTO
import io.defitrack.common.network.Network
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

@Component
class ERC20Client(
    private val erC20RestClient: ERC20RestClient
) {

    val allTokenCache = Cache.Builder<String, List<FungibleTokenInformation>>()
        .expireAfterWrite(1.hours)
        .build()

    val tokenCache = Cache.Builder<String, FungibleTokenInformation>()
        .expireAfterWrite(1.hours)
        .build()


    suspend fun getAllTokens(network: Network, verified: Boolean?): List<FungibleTokenInformation> {
        return allTokenCache.get("$network-$verified") {
            erC20RestClient.getAllTokens(network, verified)
        }
    }

    suspend fun getTokenInformation(network: Network, address: String): FungibleTokenInformation {
        return tokenCache.get("$network-$address") {
            erC20RestClient.getTokenInformation(network, address)
        }
    }

    suspend fun getWrappedToken(network: Network): WrappedTokenDTO {
        return erC20RestClient.getWrappedToken(network)
    }

    suspend fun getAllowance(network: Network, token: String, owner: String, spender: String): BigInteger {
        return erC20RestClient.getAllowance(network, token, owner, spender)
    }
}
