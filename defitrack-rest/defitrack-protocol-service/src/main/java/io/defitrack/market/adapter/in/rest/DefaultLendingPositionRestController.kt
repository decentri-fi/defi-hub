package io.defitrack.market.adapter.`in`.rest

import io.defitrack.market.adapter.`in`.mapper.LendingPositionVOMapper
import io.defitrack.market.adapter.`in`.resource.LendingPositionVO
import io.defitrack.market.port.`in`.LendingPositions
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.crypto.WalletUtils

@RestController
@RequestMapping("/{protocol}/lending")
class DefaultLendingPositionRestController(
    private val lendingPositions: LendingPositions,
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
        return lendingPositions.getPositions(protocol, address).map { lendingPositionVOMapper.map(it) }
    }
}