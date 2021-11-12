package io.codechef.protocol.beefy.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/eol")
class BeefyEOLHealthIndicator(private val quickswapHealthIndicator: QuickswapHealthIndicator) {

    @GetMapping
    fun getEOL() = quickswapHealthIndicator.getEOL()
}