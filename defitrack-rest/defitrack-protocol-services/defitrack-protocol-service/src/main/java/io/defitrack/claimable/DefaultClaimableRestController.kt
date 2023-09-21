package io.defitrack.claimable

import io.defitrack.claimable.mapper.ClaimableVOMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.crypto.WalletUtils.isValidAddress

@RestController
@RequestMapping("/{protocol}")
class DefaultClaimableRestController(
    private val claimableRewardProviders: List<ClaimableRewardProvider>,
    private val defaultClaimableRewardProvider: DefaultClaimableRewardProvider,
    private val claimableVOMapper: ClaimableVOMapper
) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping(value = ["/{address}/claimables"])
    suspend fun claimables(
        @PathVariable("protocol") protocol: String,
        @PathVariable("address") address: String,
    ): List<ClaimableVO> = coroutineScope {

        if(!isValidAddress(address)) {
            return@coroutineScope emptyList()
        }

        val fromProviders = async {
            getFromProviders(protocol, address).map { toVO(it) }
        }

        val fromDefaultProvider = async {
            getFromDefaultProvider(address).map { toVO(it) }
        }


        awaitAll(fromProviders, fromDefaultProvider).flatten().filterNotNull()
    }

    private suspend fun toVO(it: Claimable) = try {
        claimableVOMapper.map(it)
    } catch (ex: Exception) {
        logger.error("Error while fetching claimable", ex)
        null
    }

    private suspend fun getFromDefaultProvider(user: String): List<Claimable> {
        return defaultClaimableRewardProvider.claimables(user)
    }

    private suspend fun getFromProviders(protocol: String, address: String) = claimableRewardProviders
        .filter {
            it.getProtocol().slug == protocol
        }.flatMap {
            try {
                it.claimables(address)
            } catch (ex: Exception) {
                ex.printStackTrace()
                logger.error("Unable to fetch claimables for provider ${it.getProtocol().slug}")
                emptyList()
            }
        }
}