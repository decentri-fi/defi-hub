package io.defitrack.market.farming

import io.defitrack.market.farming.mapper.FarmingPositionVOMapper
import io.defitrack.market.farming.vo.FarmingPositionVO
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.web3j.crypto.WalletUtils
import java.math.BigInteger

@RestController
@RequestMapping(*["/staking", "/farming"])
class DefaultFarmingPositionRestController(
    private val farmingPositionProviders: List<FarmingPositionProvider>,
    private val farmingPositionVOMapper: FarmingPositionVOMapper
) {

    @GetMapping("/{userAddress}/positions")
    fun getPositions(@PathVariable("userAddress") address: String): List<FarmingPositionVO> = runBlocking {
        if (WalletUtils.isValidAddress(address)) {
            val results = farmingPositionProviders.flatMap {
                try {
                    it.getStakings(address).filter {
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

    @GetMapping(value = ["/{userAddress}/positions"], params = ["stakingElementId"])
    fun getStakingById(
        @PathVariable("userAddress") address: String,
        @RequestParam("stakingElementId") stakingElementId: String,
    ): FarmingPositionVO? = runBlocking {
        if (WalletUtils.isValidAddress(address)) {
            farmingPositionProviders.firstNotNullOfOrNull {
                try {
                    it.getStaking(address, stakingElementId)
                } catch (ex: Exception) {
                    logger.error("Something went wrong trying to fetch the user farms: ${ex.message}")
                    null
                }
            }?.let {
                farmingPositionVOMapper.map(it)
            }
        } else {
            null
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}