package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.erc20.protocolspecific.*
import io.defitrack.logo.LogoService
import io.defitrack.nativetoken.NativeTokenService
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class TokenService(
    private val erc20ContractReader: ERC20ContractReader,
    private val erC20Repository: ERC20Repository,
    private val LpContractReader: LpContractReader,
    private val hopTokenService: HopTokenService,
    private val curveTokenService: CurveTokenService,
    private val setProtocolTokenService: SetProtocolTokenService,
    private val balancerTokenService: BalancerTokenService,
    private val nativeTokenService: NativeTokenService,
    private val velodromeTokenService: VelodromeTokenService,
    private val kyberElasticTokenService: KyberElasticTokenService,
    private val makerTokenIdentifier: MakerTokenIdentifier,
    private val poolTogetherTokenIdentifier: PoolTogetherTokenIdentifier,
    private val tokenIdentifiers: List<TokenIdentifier>,
    private val logoService: LogoService
) {

    val logger = LoggerFactory.getLogger(this.javaClass)

    val tokenCache: Cache<String, List<TokenInformation>> = Cache.Builder().build()

   // @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun refreshCaches() {
        refreshCache()
    }

    fun refreshCache() = runBlocking {
        logger.info("refreshing token cache")
        val semaphore = Semaphore(16)
        Network.values().map { network ->
            val millis = measureTimeMillis {
                val tokens = erC20Repository.allTokens(network).map {
                    async {
                        try {
                            semaphore.withPermit {
                                getTokenInformation(it, network)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
                tokenCache.put("tokens-${network}", tokens)
            }
            logger.info("refreshing token cache for ${network} took ${millis / 1000}s")
        }
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
                totalSupply = token.totalSupply,
                type = TokenType.SINGLE,
                network = network
            )
        }
    }
}