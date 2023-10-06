package io.defitrack.market

import io.defitrack.PageUtils
import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

abstract class DefaultMarketRestController<T : DefiMarket>(
    private val marketProviders: List<MarketProvider<T>>,
    private val marketMapper: MarketVOMapper<T>
) {

    @GetMapping(value = ["/all-markets"])
    @Operation(summary = "Get all markets for a specific protocol")
    fun allMarkets(
        @PathVariable("protocol") protocol: String,
    ): List<MarketVO> {
        return getAllMarkets(protocol).map(marketMapper::map)
    }

    @GetMapping("/markets", params = ["paged"])
    fun pagedMarkets(
        @PathVariable("protocol") protocol: String,
        pageable: Pageable
    ): Page<MarketVO> {
        val allMarkets = getAllMarkets(
            protocol = protocol
        )

        return PageUtils.createPageFromList<T>(
            allMarkets, pageable
        ).map {
            marketMapper.map(it)
        }
    }

    private fun getAllMarkets(protocol: String) = marketProviders
        .filter {
            it.getProtocol().slug == protocol
        }
        .flatMap {
            it.getMarkets()
        }

}