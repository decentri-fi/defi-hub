package io.defitrack.market.lending

import io.defitrack.market.lending.mapper.LendingPositionVOMapper
import io.defitrack.market.lending.vo.LendingPositionVO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.crypto.WalletUtils

@RestController
@RequestMapping("/{protocol}/lending")
class DefaultLendingPositionRestController(
    private val lendingPositionProvider: LendingPositionProvider,
    private val lendingPositionVOMapper: LendingPositionVOMapper
) {

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