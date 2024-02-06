package io.defitrack.market.adapter.`in`.rest

import io.defitrack.PageUtils.createPageFromList
import io.defitrack.market.adapter.`in`.mapper.MarketVOMapper
import io.defitrack.market.adapter.`in`.resource.MarketVO
import io.defitrack.market.domain.DefiMarket
import io.defitrack.market.port.`in`.Markets
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.util.function.Function

abstract class DefaultMarketRestController<T : DefiMarket>(
    private val markets: Markets<T>,
    private val marketMapper: MarketVOMapper<T>,
) {

    @GetMapping(value = ["/all-markets"])
    @Operation(summary = "Get all markets for a specific protocol")
    suspend fun allMarkets(
        @PathVariable("protocol") protocol: String,
        @RequestParam("network", required = false) network: String? = null
    ): List<MarketVO> {
        return markets.getAllMarkets(protocol, network).map {
            marketMapper.map(it)
        }
    }

    @GetMapping("/markets")
    suspend fun pagedMarkets(
        @PathVariable("protocol") protocol: String,
        @RequestParam("network", required = false) network: String? = null,
        pageable: Pageable
    ): Page<MarketVO> {
        val allMarkets = markets.getAllMarkets(
            protocol = protocol,
            network = network
        )

        return createPageFromList(allMarkets, pageable).map {
           runBlocking {
               marketMapper.map(it)
           }
        }
    }


}