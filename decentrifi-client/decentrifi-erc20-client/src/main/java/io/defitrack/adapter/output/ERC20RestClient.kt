package io.defitrack.adapter.output

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.erc20.WrappedTokenDTO
import io.defitrack.common.network.Network
import io.defitrack.port.output.ERC20Client
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

@Component
class ERC20RestClient(
    private val client: HttpClient,
    @Value("\${erc20ResourceLocation:http://defitrack-erc20.default.svc.cluster.local:8080}") private val erc20ResourceLocation: String
) : ERC20Client {

    private val logger = LoggerFactory.getLogger(this::class.java)
    override suspend fun getAllTokens(network: Network, verified: Boolean?): List<FungibleTokenInformation> {
        return withContext(Dispatchers.IO) {
            val get = client.get("$erc20ResourceLocation/${network.name}") {
                parameter("verified", verified)
            }
            if (!get.status.isSuccess()) {
                emptyList()
            } else {
                get.body<List<FungibleTokenInformation>>()
            }
        }
    }


    override suspend fun getTokenInformation(network: Network, address: String): FungibleTokenInformation {
        return withContext(Dispatchers.IO) {
            val result = client.get("$erc20ResourceLocation/${network.name}/$address/token")
            if (!result.status.isSuccess()) {
                throw RuntimeException("Failed to get token information for $address")
            }
            result.body()
        }
    }

    override suspend fun getWrappedToken(network: Network): WrappedTokenDTO = withContext(Dispatchers.IO) {
        val response = client.get("$erc20ResourceLocation/${network.name}/wrapped")
        if (!response.status.isSuccess()) {
            throw RuntimeException("Failed to get wrapped token for $network")
        }
        response.body()
    }

    override suspend fun getAllowance(network: Network, token: String, owner: String, spender: String): BigInteger {
        return withContext(Dispatchers.IO) {
            val get = client.get("$erc20ResourceLocation/${network.name}/allowance/$token/$owner/$spender")
            if (get.status.isSuccess()) {
                get.body()
            } else {
                logger.debug("Failed to get allowance for {} and {} and {} on {}", token, owner, spender, network)
                BigInteger.ZERO
            }
        }
    }
}