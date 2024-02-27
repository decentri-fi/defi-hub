package io.defitrack.protocol.application.starknet

import arrow.core.nel
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.claim.*
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnCompany(Company.STARKNET)
@ConditionalOnNetwork(Network.ETHEREUM)
class StarknetAirdropProvider(
    private val httpClient: HttpClient
) : AbstractUserClaimableProvider() {

    val logger = LoggerFactory.getLogger(this::class.java)

    data class StarknetAirdropEntry(
        val address: String,
        val amount: String
    )

    @PostConstruct
    fun init() = runBlocking {
        logger.info("Starknet Airdrop Provider initialized with ${entries.await().size} entries")
    }


    val stark = lazyAsync {
        erC20Resource.getTokenInformation(getNetwork(), "0xca14007eff0db1f8135f4c25b34de49ab0d42766")
    }

    val entries = lazyAsync {
        val result: String =
            httpClient.get("https://raw.githubusercontent.com/decentri-fi/data/master/csv/starknet.csv").bodyAsText()
        result.lines().drop(1).filter {
            it.isNotBlank()
        }.associate {
            it.split(",")[0].lowercase() to it.split(",")[1]
        }
    }

    override suspend fun claimables(address: String): List<UserClaimable> {
        return entries.await().get(address.lowercase())?.let {
            UserClaimable(
                id = "starknet-airdrop",
                name = "Starknet Airdrop",
                protocol = getProtocol(),
                network = getNetwork(),
                amount = BigDecimal.valueOf(it.toDouble()).times(BigDecimal.valueOf(10).pow(18))
                    .toBigInteger(),
                claimableToken = stark.await()
            ).nel()
        } ?: emptyList()
    }

    override fun getProtocol(): Protocol {
        return Protocol.STARKNET
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}