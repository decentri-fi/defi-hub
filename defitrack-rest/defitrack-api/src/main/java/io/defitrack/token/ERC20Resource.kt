package io.defitrack.token

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGateway.Companion.MAX_UINT256
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Function
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

@Component
class ERC20Resource(
    private val client: HttpClient,
    private val abiResource: ABIResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    @Value("\${erc20ResourceLocation:http://defitrack-erc20:8080}") private val erc20ResourceLocation: String
) {

    val semaphore = Semaphore(4)

    val tokensCache: Cache<String, List<TokenInformationVO>> = Cache.Builder()
        .expireAfterWrite(1.hours)
        .build()

    val tokenCache: Cache<String, TokenInformationVO> = Cache.Builder()
        .expireAfterWrite(1.hours)
        .build()

    val wrappedCache: Cache<Network, WrappedToken> = Cache.Builder().build()

    val erc20ABI by lazy {
        runBlocking {
            abiResource.getABI("general/ERC20.json")
        }
    }

    suspend fun getAllTokens(network: Network): List<TokenInformationVO> = withContext(Dispatchers.IO) {
        tokensCache.get("tokens-${network}") {
            retry(limitAttempts(3)) { client.get("$erc20ResourceLocation/${network.name}").body() }
        }
    }

    suspend fun getBalance(network: Network, tokenAddress: String, user: String): BigInteger =
        withContext(Dispatchers.IO) {
            semaphore.withPermit {
                client.get("$erc20ResourceLocation/${network.name}/$tokenAddress/$user").body()
            }
        }

    suspend fun getTokenInformation(network: Network, address: String): TokenInformationVO {
        return withContext(Dispatchers.IO) {
            tokenCache.get("token-${network}-${address}") {
                semaphore.withPermit {
                    retry(limitAttempts(3)) { client.get("$erc20ResourceLocation/${network.name}/$address/token").body() }
                }
            }
        }
    }

    suspend fun getWrappedToken(network: Network) : WrappedToken{
        return withContext(Dispatchers.IO) {
            wrappedCache.get(network) {
                retry(limitAttempts(3)) { client.get("$erc20ResourceLocation/${network.name}/wrapped").body() }
            }
        }
    }

    fun getApproveFunction(
        network: Network,
        token: String,
        spender: String,
        amount: BigInteger
    ): Function {
        return with(blockchainGatewayProvider.getGateway(network)) {
            ERC20Contract(
                this,
                erc20ABI,
                token
            ).approveFunction(spender, amount)
        }
    }

    fun getFullApproveFunction(
        network: Network,
        token: String,
        spender: String
    ): Function {
        return getApproveFunction(network, token, spender, MAX_UINT256.value)
    }

    suspend fun getAllowance(network: Network, token: String, owner: String, spender: String): BigInteger {
        return with(blockchainGatewayProvider.getGateway(network)) {
            ERC20Contract(
                this,
                erc20ABI,
                token
            ).allowance(owner, spender)
        }
    }

    fun balanceOfFunction(token: String, user: String, network: Network): Function {
        return ERC20Contract(
            blockchainGatewayProvider.getGateway(network),
            erc20ABI,
            token
        ).balanceOfMethod(user)
    }

    suspend fun getBalancesFor(
        address: String,
        tokens: List<String>,
        network: Network,
    ): List<BigInteger> {
        with(blockchainGatewayProvider.getGateway(network)) {
            return readMultiCall(tokens.map {
                MultiCallElement(
                    ERC20Contract(
                        this,
                        erc20ABI,
                        it
                    ).balanceOfMethod(address),
                    it
                )
            }).map {
                try {
                    it[0].value as BigInteger
                } catch (_: Exception) {
                    BigInteger.ZERO
                }
            }
        }
    }

    data class WrappedToken(
        val address: String
    )
}