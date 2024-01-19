package io.defitrack.market.adapter.`in`.rest

import io.defitrack.market.adapter.`in`.mapper.FarmingPositionVOMapper
import io.defitrack.market.adapter.`in`.resource.FarmingPositionVO
import io.defitrack.market.port.out.FarmingPositionProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.web3j.crypto.WalletUtils
import java.math.BigInteger

@RestController
@RequestMapping("/{protocol}/staking", "/{protocol}/farming")
class DefaultFarmingPositionRestController(
    private val farmingPositionProviders: List<FarmingPositionProvider>,
    private val farmingPositionVOMapper: FarmingPositionVOMapper
) {

    @GetMapping("/{userAddress}/positions")
    suspend fun getPositions(
        @PathVariable("protocol") protocol: String,
        @PathVariable("userAddress") address: String
    ): List<FarmingPositionVO> {
        return if (WalletUtils.isValidAddress(address)) {
            val results = farmingPositionProviders
                .flatMap {
                    try {
                        it.getStakings(protocol, address).filter {
                            it.underlyingAmount > BigInteger.ZERO
                        }
                    } catch (ex: Exception) {
                        logger.error("Something went wrong trying to fetch the user stakings: ${ex.message}")
                        emptyList()
                    }
                }.map {
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