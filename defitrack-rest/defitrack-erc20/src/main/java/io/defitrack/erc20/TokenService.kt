package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.erc20.protocolspecific.TokenIdentifier
import io.defitrack.logo.LogoService
import io.defitrack.nativetoken.NativeTokenService
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import io.github.reactivecircus.cache4k.Cache
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class TokenService(
    private val erc20ContractReader: ERC20ContractReader,
    private val erC20Repository: ERC20Repository,
    private val nativeTokenService: NativeTokenService,

    private val logoService: LogoService
) {

    @Autowired
    private lateinit var tokenIdentifiers: List<TokenIdentifier>

    val logger = LoggerFactory.getLogger(this.javaClass)
    val tokenCache: Cache<String, List<TokenInformation>> = Cache.Builder().build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3, initialDelay = 1000 * 60 * 60 * 3)
    fun refreshCaches() = runBlocking {
        refreshCache()
    }

    suspend fun refreshCache() {
        tokenCache.asMap().entries.flatMap {
            it.value
        }.forEach {
            it.refresh()
        }
    }

    @PostConstruct
    fun populateCaches() = runBlocking {
        initialPopulation()
    }

    fun initialPopulation() = runBlocking {
        logger.info("refreshing token cache")
        val semaphore = Semaphore(8)
        Network.values().map { network ->
            val millis = measureTimeMillis {
                val allTokens = erC20Repository.allTokens(network)
                logger.info("found ${allTokens.size} tokens for network ${network.name}. Importing.")
                val tokens = allTokens.map {
                    async {
                        try {
                            semaphore.withPermit {
                                getTokenInformation(it, network)
                            }
                        } catch (ex: Exception) {
                            logger.error("Unable to import token $it on network $network", ex)
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
                tokenCache.put("tokens-${network}", tokens)
            }
            logger.info("refreshing token cache for $network took ${millis / 1000}s (${tokenCache.get("tokens-${network}")?.size ?: 0} tokens)")
        }
        logger.info("done refreshing tokens")
    }


    suspend fun getAllTokensForNetwork(network: Network): List<TokenInformation> {
        return tokenCache.get("tokens-${network}") ?: emptyList()
    }

    val tokenInformationCache = Cache.Builder().build<String, TokenInformation>()

    suspend fun getTokenInformation(address: String, network: Network): TokenInformation {
        if (address == "0x0" || address.lowercase() == "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee") {
            return nativeTokenService.getNativeToken(network)
        }
        return tokenInformationCache.get("${address}-${network}") {
            val token = erc20ContractReader.getERC20(network, address)

            tokenIdentifiers.find { it.isProtocolToken(token) }?.getTokenInfo(token) ?: TokenInformation(
                logo = logoService.generateLogoUrl(network, address),
                name = token.name,
                symbol = token.symbol,
                address = token.address,
                decimals = token.decimals,
                totalSupply = refreshable(token.totalSupply) {
                    erc20ContractReader.getERC20(network, address).totalSupply
                },
                type = TokenType.SINGLE,
                network = network
            )
        }
    }
}