package io.defitrack.market.lending

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.lending.domain.LendingPosition
import io.defitrack.market.lending.vo.LendingPositionVO
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.mapper.ProtocolVOMapper
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.web3j.crypto.WalletUtils

@RestController
@RequestMapping("/lending")
class DefaultLendingPositionRestController(
    private val lendingPositionProvider: LendingPositionProvider,
    private val priceResource: PriceResource,
    private val protocolVOMapper: ProtocolVOMapper
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping("/{userId}/positions")
    fun getPoolingMarkets(@PathVariable("userId") address: String): List<LendingPositionVO> = runBlocking {
        lendingPositionProvider.getLendings(address).map {
            it.toVO()
        }
    }

    @GetMapping(value = ["/{userId}/positions"], params = ["lendingElementId", "network"])
    fun getLendingById(
        @PathVariable("userId") address: String,
        @RequestParam("lendingElementId") lendingElementId: String,
        @RequestParam("network") network: Network
    ): LendingPositionVO? = runBlocking {
        if (WalletUtils.isValidAddress(address)) {
            try {
                lendingPositionProvider.getLending(address, lendingElementId)?.toVO()
            } catch (ex: Exception) {
                logger.error("Something went wrong trying to fetch the user farms: ${ex.message}")
                null
            }
        } else {
            null
        }
    }

    suspend fun LendingPosition.toVO(): LendingPositionVO {
        return with(this) {

            val lendingInDollars = priceResource.calculatePrice(
                PriceRequest(
                    address = market.token.address,
                    network = market.network,
                    amount = underlyingAmount.asEth(market.token.decimals),
                    type = null
                )
            )

            LendingPositionVO(
                network = market.network.toVO(),
                protocol = protocolVOMapper.map(market.protocol),
                dollarValue = lendingInDollars,
                rate = market.rate?.toDouble(),
                name = market.name,
                amountDecimal = underlyingAmount.asEth(market.token.decimals).toDouble(),
                id = market.id,
                token = market.token,
                exitPositionSupported = market.exitPositionPreparer !== null,
                amount = tokenAmount,
            )
        }
    }
}