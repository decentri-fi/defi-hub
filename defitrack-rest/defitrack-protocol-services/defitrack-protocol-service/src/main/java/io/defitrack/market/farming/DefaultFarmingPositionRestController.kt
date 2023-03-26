package io.defitrack.market.farming

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.farming.FarmingPositionProvider
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.market.farming.vo.FarmingPositionVO
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.toVO
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.web3j.crypto.WalletUtils
import java.math.BigInteger

@RestController
@RequestMapping(*["/staking", "/farming"])
class DefaultFarmingPositionRestController(
    private val stakingServices: List<FarmingPositionProvider>,
    private val priceResource: PriceResource
) {

    @GetMapping("/{userAddress}/positions")
    fun getPositions(@PathVariable("userAddress") address: String): List<FarmingPositionVO> = runBlocking {
        if (WalletUtils.isValidAddress(address)) {
            val results = stakingServices.flatMap {
                try {
                    it.getStakings(address).filter {
                        it.underlyingAmount > BigInteger.ZERO
                    }
                } catch (ex: Exception) {
                    logger.error("Something went wrong trying to fetch the user stakings: ${ex.message}")
                    emptyList()
                }
            }.map {
                it.toVO()
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
            stakingServices.firstNotNullOfOrNull {
                try {
                    it.getStaking(address, stakingElementId)
                } catch (ex: Exception) {
                    logger.error("Something went wrong trying to fetch the user farms: ${ex.message}")
                    null
                }
            }?.toVO()
        } else {
            null
        }
    }


    suspend fun FarmingPosition.toVO(): FarmingPositionVO {
        val stakedInDollars = priceResource.calculatePrice(
            PriceRequest(
                address = market.stakedToken.address,
                network = market.network,
                amount = underlyingAmount.asEth(market.stakedToken.decimals),
                type = market.stakedToken.type
            )
        )

        return FarmingPositionVO(
            id = market.id,
            network = market.network.toVO(),
            protocol = market.protocol.toVO(),
            dollarValue = stakedInDollars,
            name = market.name,
            apr = market.apr?.toDouble(),
            vaultType = market.contractType,
            stakedToken = market.stakedToken,
            rewardTokens = market.rewardTokens,
            stakedAmountDecimal = underlyingAmount.asEth(market.stakedToken.decimals),
            stakedAmount = underlyingAmount.toString(10),
            exitPositionSupported = market.exitPositionPreparer != null,
            tokenAmount = tokenAmount.toString(10),
            tokenAmountDecimal = tokenAmount.asEth(market.stakedToken.decimals),
        )
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}