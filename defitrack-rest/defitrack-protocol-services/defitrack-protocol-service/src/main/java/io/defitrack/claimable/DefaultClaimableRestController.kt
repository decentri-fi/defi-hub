package io.defitrack.claimable

import io.defitrack.claimable.mapper.ClaimableVOMapper
import io.defitrack.price.PriceResource
import io.defitrack.protocol.mapper.ProtocolVOMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/{protocol}")
class DefaultClaimableRestController(
    private val claimableRewardProviders: List<ClaimableRewardProvider>,
    private val priceResource: PriceResource,
    private val defaultClaimableRewardProvider: DefaultClaimableRewardProvider,
    private val protocolVOMapper: ProtocolVOMapper,
    private val claimableVOMapper: ClaimableVOMapper
) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping(value = ["/{address}/claimables"])
    fun claimables(
        @PathVariable("protocol") protocol: String,
        @PathVariable("address") address: String,
    ): List<ClaimableVO> = runBlocking {
        val fromProviders = async {
            getFromProviders(protocol, address).map { toVO(it) }
        }

        val fromDefaultProvider = async {
            getFromDefaultProvider(address).map { toVO(it) }
        }

        (fromProviders.await() + fromDefaultProvider.await()).filterNotNull()
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
            it.claimables(address)
        }
}