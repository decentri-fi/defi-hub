package io.defitrack.market.adapter.`in`.rest

import io.defitrack.market.adapter.`in`.mapper.FarmingPositionVOMapper
import io.defitrack.market.adapter.`in`.resource.FarmingPositionVO
import io.defitrack.market.port.`in`.FarmingPositions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.crypto.WalletUtils

@RestController
@RequestMapping("/{protocol}/staking", "/{protocol}/farming")
class DefaultFarmingPositionRestController(
    private val farmingPositions: FarmingPositions,
    private val farmingPositionVOMapper: FarmingPositionVOMapper
) {

    @GetMapping("/{userAddress}/positions")
    suspend fun getPositions(
        @PathVariable("protocol") protocol: String,
        @PathVariable("userAddress") address: String
    ): List<FarmingPositionVO> {
        return if (WalletUtils.isValidAddress(address)) {
            val results = farmingPositions.getPositions(protocol, address).map {
                farmingPositionVOMapper.map(it)
            }
            results
        } else {
            emptyList()
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}