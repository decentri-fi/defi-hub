package io.defitrack.market.lending

import io.defitrack.common.network.Network
import io.defitrack.market.lending.mapper.LendingPositionVOMapper
import io.defitrack.market.lending.vo.LendingPositionVO
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.web3j.crypto.WalletUtils

@RestController
@RequestMapping("/{protocol}/lending")
class DefaultLendingPositionRestController(
    private val lendingPositionProvider: LendingPositionProvider,
    private val lendingPositionVOMapper: LendingPositionVOMapper
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping("/{userId}/positions")
    fun getPoolingMarkets(
        @PathVariable("protocol") protocol: String,
        @PathVariable("userId") address: String
    ): List<LendingPositionVO> = runBlocking {
        lendingPositionProvider.getLendings(address).map { lendingPositionVOMapper.map(it) }
    }

    @GetMapping(value = ["/{userId}/positions"], params = ["lendingElementId", "network"])
    fun getLendingById(
        @PathVariable("userId") address: String,
        @RequestParam("lendingElementId") lendingElementId: String,
        @RequestParam("network") network: Network
    ): LendingPositionVO? = runBlocking {
        if (WalletUtils.isValidAddress(address)) {
            try {
                lendingPositionProvider.getLending(address, lendingElementId)?.let {
                    lendingPositionVOMapper.map(it)
                }
            } catch (ex: Exception) {
                logger.error("Something went wrong trying to fetch the user farms: ${ex.message}")
                null
            }
        } else {
            null
        }
    }

}