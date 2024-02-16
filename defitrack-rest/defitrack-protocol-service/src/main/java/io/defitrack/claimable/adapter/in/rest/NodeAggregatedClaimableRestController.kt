package io.defitrack.claimable.adapter.`in`.rest

import arrow.fx.coroutines.parZip
import io.defitrack.claim.AbstractUserClaimableProvider
import io.defitrack.claim.DefaultUserClaimableProvider
import io.defitrack.claim.UserClaimable
import io.defitrack.claimable.adapter.`in`.ClaimableVOMapper
import io.defitrack.claimable.vo.UserClaimableVO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.web3j.crypto.WalletUtils

@RestController
class NodeAggregatedClaimableRestController(
    private val defaultUserClaimableProvider: DefaultUserClaimableProvider,
    private val claimableVOMapper: ClaimableVOMapper,
    private val userClaimableProviders: List<AbstractUserClaimableProvider>,
) {

    @GetMapping(value = ["/claimables/{address}"])
    suspend fun claimables(
        @PathVariable("address") address: String,
        @RequestParam("include", required = false) include: List<String> = emptyList(),
    ): List<UserClaimableVO> = coroutineScope {

        if (!WalletUtils.isValidAddress(address)) {
            return@coroutineScope emptyList()
        }

        val fromProviders =
            getFromProviders(include, address).map { toVO(it) }

        val fromDefaultProvider =
            getFromDefaultProvider(address, include).map { toVO(it) }

        parZip({
            fromProviders
        }, {
            fromDefaultProvider
        }) { l1, l2 -> l1 + l2 }.filterNotNull()
    }

    private suspend fun getFromDefaultProvider(user: String, protocols: List<String>): List<UserClaimable> {
        return defaultUserClaimableProvider.claimables(user, protocols)
    }

    private suspend fun getFromProviders(include: List<String>, address: String) = userClaimableProviders
        .filter {
            include.isEmpty() || include.contains(it.getProtocol().slug)
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