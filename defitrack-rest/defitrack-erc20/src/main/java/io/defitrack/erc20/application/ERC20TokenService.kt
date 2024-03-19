package io.defitrack.erc20.application

import arrow.core.Either.Companion.catch
import arrow.core.Option
import arrow.core.flatten
import arrow.core.some
import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.domain.TokenInformation
import io.defitrack.erc20.application.repository.NativeTokenRepository
import io.defitrack.erc20.application.protocolspecific.TokenIdentifier
import io.defitrack.erc20.application.repository.ERC20TokenListResource
import io.defitrack.erc20.port.input.AllowanceUseCase
import io.defitrack.erc20.port.input.TokenInformationUseCase
import io.defitrack.erc20.port.output.ReadERC20Port
import io.defitrack.token.TokenType
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.time.measureTimedValue

@Service
class ERC20TokenService(
    private val readERC20Port: ReadERC20Port,
    private val erC20Repository: ERC20TokenListResource,
    private val nativeTokenRepository: NativeTokenRepository,
    private val logoGenerator: LogoGenerator
) : TokenInformationUseCase {

    @Autowired
    private lateinit var tokenIdentifiers: List<TokenIdentifier>

    val logger = LoggerFactory.getLogger(this.javaClass)
    val tokenInformationCache = Cache.Builder<String, Option<TokenInformation>>().build()

    override fun getAllSingleTokens(network: Network, verified: Boolean): List<TokenInformation> {
        return tokenInformationCache.asMap().filter {
            it.value.isSome()
        }.filter {
            network == it.value.getOrNull()?.network
        }.mapNotNull {
            it.value.getOrNull()
        }.distinctBy {
            it.address.lowercase() + "-" + it.network.name
        }.filter {
            it.verified == verified
        }.filter {
            it.type == TokenType.SINGLE
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


    override suspend fun getTokenInformation(
        address: String,
        network: Network,
        verified: Boolean
    ): Option<TokenInformation> {
        if (address == "0x0" || address.lowercase() == "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee") {
            return nativeTokenRepository.getNativeToken(network).some()
        }
        return tokenInformationCache.get(createIndex(address, network)) {
            fetchTokenInfo(network, address, verified)
        }
    }

    private suspend fun fetchTokenInfo(
        network: Network,
        address: String,
        verified: Boolean = false
    ): Option<TokenInformation> = catch {
        readERC20Port.getERC20(network, address).map { token ->
            tokenIdentifiers.find { it.isProtocolToken(token) }?.getTokenInfo(token) ?: singleERC20(token)
        }.map {
            it.copy(verified = verified)
        }
    }.mapLeft {
        logger.error("Error getting token information for $address on $network: {}", it.message)
    }.getOrNone().flatten()

    fun createIndex(address: String, network: Network): String {
        return "${address.lowercase()}-$network"
    }

    private fun singleERC20(token: ERC20) = TokenInformation(
        logo = logoGenerator.generateLogoUrl(token.network, token.address),
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