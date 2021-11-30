package io.defitrack.protocol.beefy.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/eol")
class BeefyEOLHealthIndicator(
    private val quickswapHealthIndicator: QuickswapHealthIndicator,
    private val sushiPolygonHealthIndicator: SushiPolygonHealthIndicator
) {

    @GetMapping("/quickswap")
    fun getEOL() = quickswapHealthIndicator.getEOL()

    @GetMapping("/sushi")
    fun sushi(): List<String> {
        return sushiPolygonHealthIndicator.getEOL()
    }
}