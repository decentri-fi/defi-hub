package io.defitrack.market

import io.defitrack.PageUtils
import io.defitrack.PageUtils.createPageFromList
import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

abstract class DefaultMarketRestController<T : DefiMarket>(
    private val marketProviders: List<MarketProvider<T>>,
    private val marketMapper: MarketVOMapper<T>
) {

    @GetMapping(value = ["/all-markets"])
    @Operation(summary = "Get all markets for a specific protocol")
    fun allMarkets(
        @PathVariable("protocol") protocol: String,
        @RequestParam("network", required = false) network: String? = null
    ): List<MarketVO> {
        return getAllMarkets(protocol, network).map(marketMapper::map)
    }

    @GetMapping("/markets")
    fun pagedMarkets(
        @PathVariable("protocol") protocol: String,
        @RequestParam("network", required = false) network: String? = null,
        pageable: Pageable
    ): Page<MarketVO> {
        val allMarkets = getAllMarkets(
            protocol = protocol,
            network = network
        )

        return createPageFromList(allMarkets, pageable).map {
            marketMapper.map(it)
        }
    }

    private fun getAllMarkets(protocol: String, network: String? = null) = marketProviders
        .filter {
            network == null || it.getNetwork().slug == network || it.getNetwork().name == network
        }
        .filter {
            it.getProtocol().slug == protocol
        }
        .flatMap {
            it.getMarkets()
        }

}