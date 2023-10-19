package io.defitrack.claimable

import io.defitrack.claimable.domain.UserClaimable
import io.defitrack.claimable.mapper.ClaimableVOMapper
import io.defitrack.claimable.vo.UserClaimableVO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.web3j.crypto.WalletUtils

@RestController
class NodeAggregatedClaimableRestController(
    private val defaultUserClaimableProvider: DefaultUserClaimableProvider,
    private val claimableVOMapper: ClaimableVOMapper,
    private val userClaimableProviders: List<UserClaimableProvider>,
) {

    @GetMapping(value = ["/claimables/{address}"])
    suspend fun claimables(
        @PathVariable("address") address: String,
    ): List<UserClaimableVO> = coroutineScope {

        if (!WalletUtils.isValidAddress(address)) {
            return@coroutineScope emptyList()
        }

        val fromProviders = async {
            getFromProviders(null, address).map { toVO(it) }
        }

        val fromDefaultProvider = async {
            getFromDefaultProvider(address, null).map { toVO(it) }
        }


        awaitAll(fromProviders, fromDefaultProvider).flatten().filterNotNull()
    }

    private suspend fun getFromDefaultProvider(user: String, protocol: String? = null): List<UserClaimable> {
        return defaultUserClaimableProvider.claimables(user, protocol)
    }

    private suspend fun getFromProviders(protocol: String? = null, address: String) = userClaimableProviders
        .filter {
            protocol == null || it.getProtocol().slug == protocol
        }.flatMap {
            try {
                it.claimables(address)
            } catch (ex: Exception) {
                ex.printStackTrace()
                DefaultClaimableRestController.logger.error("Unable to fetch claimables for provider ${it.getProtocol().slug}")
                emptyList()
            }
        }

    private suspend fun toVO(it: UserClaimable) = try {
        claimableVOMapper.map(it)
    } catch (ex: Exception) {
        DefaultClaimableRestController.logger.error("Error while fetching claimable", ex)
        null
    }

}