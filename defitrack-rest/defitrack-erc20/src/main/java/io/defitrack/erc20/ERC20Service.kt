package io.defitrack.erc20

import arrow.core.Either.Companion.catch
import arrow.core.Option
import arrow.core.flatten
import arrow.core.some
import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.erc20.logo.LogoService
import io.defitrack.erc20.nativetoken.NativeTokenService
import io.defitrack.erc20.protocolspecific.TokenIdentifier
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.time.measureTimedValue

@Service
class ERC20Service(
    private val erc20ContractReader: ERC20ContractReader,
    private val erC20Repository: ERC20Repository,
    private val nativeTokenService: NativeTokenService,
    private val logoService: LogoService
) {

    @Autowired
    private lateinit var tokenIdentifiers: List<TokenIdentifier>

    val logger = LoggerFactory.getLogger(this.javaClass)
    val tokenInformationCache = Cache.Builder<String, Option<TokenInformation>>().build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3, initialDelay = 1000 * 60 * 60 * 3)
    fun refreshCaches() = runBlocking {
        refreshCache()
    }

    fun getAllTokensForNetwork(network: Network): List<TokenInformation> {
        return tokenInformationCache.asMap().filter {
            it.value.isSome()
        }.filter {
            network == it.value.getOrNull()?.network
        }.mapNotNull {
            it.value.getOrNull()
        }.distinctBy {
            it.address.lowercase() + "-" + it.network.name
        }
    }

    suspend fun refreshCache() {
        logger.info("refreshing token caches")
        tokenInformationCache.asMap().entries.forEach { entry ->
            entry.value.onSome { tokenInfo ->
                tokenInfo.refresh()
            }
        }
        logger.info("done refreshing token caches")
    }

    suspend fun initialPopulation() = coroutineScope {
        Network.entries.map { network ->
            val timedvalue = measureTimedValue {
                val allTokens = erC20Repository.allTokens(network)
                logger.info("found ${allTokens.size} tokens for network ${network.name}. Importing.")
                allTokens.parMap(concurrency = 12) {
                    fetchTokenInfo(network, it, true)
                }.filter {
                    it.isSome()
                }.forEach {
                    tokenInformationCache.put(createIndex(it.getOrNull()!!.address, network), it)
                }
            }
            logger.info("refreshing token cache for $network took ${timedvalue.duration.inWholeSeconds}s (${tokenInformationCache.asMap().size}) tokens)")
        }
    }


    suspend fun getTokenInformation(
        address: String,
        network: Network,
        verified: Boolean = false
    ): Option<TokenInformation> {
        if (address == "0x0" || address.lowercase() == "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee") {
            return nativeTokenService.getNativeToken(network).some()
        }
        return tokenInformationCache.get(createIndex(address, network)) {
            fetchTokenInfo(network, address)
        }
    }

    private suspend fun fetchTokenInfo(
        network: Network,
        address: String,
        verified: Boolean = false
    ): Option<TokenInformation> = catch {
        erc20ContractReader.getERC20(network, address).map { token ->
            tokenIdentifiers.find { it.isProtocolToken(token) }?.getTokenInfo(token) ?: singleERC20(token)
        }.map {
            it.copy(verified = verified)
        }
    }.mapLeft {
        logger.error("Error getting token information for $address on $network", it)
    }.getOrNone().flatten()

    fun createIndex(address: String, network: Network): String {
        return "${address.lowercase()}-$network"
    }

    private fun singleERC20(token: ERC20) = TokenInformation(
        logo = logoService.generateLogoUrl(token.network, token.address),
        name = token.name,
        symbol = token.symbol,
        address = token.address,
        decimals = token.decimals,
        totalSupply = token.totalSupply,
        type = TokenType.SINGLE,
        network = token.network,
        verified = false
    )
}