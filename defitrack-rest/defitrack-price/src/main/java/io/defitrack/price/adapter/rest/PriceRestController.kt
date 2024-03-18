package io.defitrack.price.adapter.rest

import io.defitrack.common.network.Network
import io.defitrack.price.application.PriceAggregator
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.external.domain.NO_EXTERNAL_PRICE
import io.defitrack.price.port.`in`.PriceCalculator
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/")
class PriceRestController(
    private val priceCalculator: PriceCalculator,
    private val priceAggregator: PriceAggregator
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    suspend fun calculatePrice(@RequestBody getPriceCommand: GetPriceCommand): Double {
        return priceCalculator.calculatePrice(getPriceCommand)
    }

    @GetMapping
    suspend fun getAllPrices(
        @RequestParam("network", required = false) networkName: String? = null,
        @RequestParam("type", required = false) type: String? = null,
    ): List<ExternalPrice> {
        return priceAggregator.getAllPrices()
            .filter {
                networkName == null || networkName.lowercase() == it.network.slug
            }.filter {
                type == null || type.lowercase() == it.source
            }
    }


    @GetMapping("/{address}")
    suspend fun getPricePerToken(
        @PathVariable("address") address: String,
        @RequestParam("network") networkName: String
    ): ExternalPrice {
        val network = Network.fromString(networkName) ?: return NO_EXTERNAL_PRICE
        val alternatives = getAlternatives(address, network)
        return try {
            priceAggregator.getAllPrices().find {
                alternatives.contains(it.address.lowercase())
            } ?: return NO_EXTERNAL_PRICE
        } catch (ex: Exception) {
            logger.error("Error calculating price for $address on $networkName", ex)
            return NO_EXTERNAL_PRICE
        }
    }

    fun getAlternatives(address: String, network: Network): List<String> {
        val entries = alternatives[network]
        return entries?.values?.first { all ->
            all.contains(address.lowercase())
        } ?: listOf(address.lowercase())
    }

    val alternatives = mapOf(
        Network.ETHEREUM to mapOf(
            "ether" to listOf(
                "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2",
                "0x0000000000000000000000000000000000000000"
            ).map(String::lowercase),
        ),
        Network.ARBITRUM to mapOf(
            "usdc" to listOf(
                "0xff970a61a04b1ca14834a43f5de4533ebddb5cc8",
                "0xaf88d065e77c8cc2239327c5edb3a432268e5831"
            ),
            "ether" to listOf(
                "0x82af49447d8a07e3bd95bd0d56f35241523fbab1",
                "0x0000000000000000000000000000000000000000"
            )
        ),
        Network.OPTIMISM to mapOf(
            "ether" to listOf(
                "0x4200000000000000000000000000000000000006",
                "0x0000000000000000000000000000000000000000"
            )
        ),
        Network.BASE to mapOf(
            "ether" to listOf(
                "0x4200000000000000000000000000000000000006",
                "0x0000000000000000000000000000000000000000"
            )
        )
    )
}