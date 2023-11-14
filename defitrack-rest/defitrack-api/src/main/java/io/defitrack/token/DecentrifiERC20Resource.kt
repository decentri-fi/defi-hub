package io.defitrack.token

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.multicall.MultiCallElement
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

@Component
class DecentrifiERC20Resource(
    private val client: HttpClient,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    @Value("\${erc20ResourceLocation:http://defitrack-erc20.default.svc.cluster.local:8080}") private val erc20ResourceLocation: String
) : ERC20Resource {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val tokensCache = Cache.Builder<String, List<TokenInformationVO>>().expireAfterWrite(1.hours).build()

    val tokenCache = Cache.Builder<String, TokenInformationVO>().expireAfterWrite(1.hours).build()

    val wrappedCache = Cache.Builder<Network, WrappedToken>().build()

    override suspend fun getAllTokens(network: Network, verified: Boolean?): List<TokenInformationVO> =
        withContext(Dispatchers.IO) {
            tokensCache.get("tokens-${network}") {
                val get = client.get("$erc20ResourceLocation/${network.name}") {
                    parameter("verified", verified)
                }
                if (!get.status.isSuccess()) {
                    emptyList()
                } else {
                    get.body<List<TokenInformationVO>>()
                }
            }
        }

    override suspend fun getBalance(network: Network, tokenAddress: String, user: String): BigInteger =
        withContext(Dispatchers.IO) {
            val get = client.get("$erc20ResourceLocation/${network.name}/$tokenAddress/$user")
            if (get.status.isSuccess()) {
                get.body()
            } else {
                logger.debug("Failed to get balance for $tokenAddress and $user")
                BigInteger.ZERO
            }
        }

    override suspend fun getTokenInformation(network: Network, address: String): TokenInformationVO {
        return withContext(Dispatchers.IO) {
            tokenCache.get("token-${network}-${address}") {
                val result = client.get("$erc20ResourceLocation/${network.name}/$address/token")
                if (!result.status.isSuccess()) {
                    throw RuntimeException("Failed to get token information for $address")
                }
                result.body()
            }
        }
    }

    override suspend fun getWrappedToken(network: Network): WrappedToken = withContext(Dispatchers.IO) {
        wrappedCache.get(network) {
            retry(limitAttempts(3)) {
                val result = client.get("$erc20ResourceLocation/${network.name}/wrapped")
                if (!result.status.isSuccess()) {
                    throw RuntimeException("Failed to get wrapped token for $network")
                }
                result.body()
            }
        }
    }

    override suspend fun getAllowance(network: Network, token: String, owner: String, spender: String): BigInteger {
        return with(blockchainGatewayProvider.getGateway(network)) {
            ERC20Contract(
                this, token
            ).readAllowance(owner, spender)
        }
    }

    override suspend fun getBalancesFor(
        address: String,
        tokens: List<String>,
        network: Network,
    ): List<BigInteger> {
        with(blockchainGatewayProvider.getGateway(network)) {
            return readMultiCall(tokens.map {
                MultiCallElement(
                    ERC20Contract.balanceOfFunction(address), it
                )
            }).map {
                try {
                    if (!it.success) {
                        BigInteger.ZERO
                    } else {
                        it.data[0].value as BigInteger
                    }
                } catch (_: Exception) {
                    BigInteger.ZERO
                }
            }
        }
    }
}