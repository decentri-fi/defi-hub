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
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

@Component
class ERC20Resource(
    private val client: HttpClient,
    private val abiResource: ABIResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    @Value("\${erc20ResourceLocation:http://defitrack-erc20:8080}") private val erc20ResourceLocation: String
) {


    val tokenCache: Cache<String, List<TokenInformationVO>> = Cache.Builder().build()


    val erc20ABI by lazy {
        abiResource.getABI("general/ERC20.json")
    }

    suspend fun getAllTokens(network: Network): List<TokenInformationVO> = withContext(Dispatchers.IO) {
        tokenCache.get("tokens-${network}") {
            retry(limitAttempts(3)) { client.get("$erc20ResourceLocation/${network.name}").body() }
        }
    }

    suspend fun getBalance(network: Network, tokenAddress: String, user: String): BigInteger =
        withContext(Dispatchers.IO) {
            client.get("$erc20ResourceLocation/${network.name}/$tokenAddress/$user").body()
        }

    suspend fun getTokenInformation(network: Network, address: String): TokenInformationVO =
        withContext(Dispatchers.IO) {
            retry(limitAttempts(3)) { client.get("$erc20ResourceLocation/${network.name}/$address/token").body() }
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
}