package io.defitrack.market.lending

import io.defitrack.common.network.Network
import io.defitrack.market.lending.mapper.LendingPositionVOMapper
import io.defitrack.market.lending.vo.LendingPositionVO
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
    suspend fun getPoolingMarkets(
        @PathVariable("protocol") protocol: String,
        @PathVariable("userId") address: String
    ): List<LendingPositionVO> {
        if (!WalletUtils.isValidAddress(address)) {
            return emptyList()
        }
        return lendingPositionProvider.getLendings(protocol, address).map { lendingPositionVOMapper.map(it) }
    }
}