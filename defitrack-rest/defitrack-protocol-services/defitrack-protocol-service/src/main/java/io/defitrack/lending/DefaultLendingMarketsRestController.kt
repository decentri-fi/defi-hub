package io.defitrack.lending

import io.defitrack.common.network.Network
import io.defitrack.lending.domain.LendingMarketElement
import io.defitrack.lending.vo.LendingMarketElementToken
import io.defitrack.lending.vo.LendingMarketElementVO
import io.defitrack.network.toVO
import io.defitrack.protocol.toVO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/lending")
class DefaultLendingMarketsRestController(
    private val lendingMarketServices: List<LendingMarketService>,
) {

    @GetMapping(value = ["/all-markets"])
    fun getAllMarkets(): List<LendingMarketElementVO> {
        return lendingMarketServices.flatMap {
            it.getLendingMarkets()
        }.map {
            it.toVO()
        }
    }

    @GetMapping(value = ["/markets"], params = ["token"])
    fun searchByToken(
        @RequestParam("token") token: String,
        @RequestParam("network") network: Network
    ): List<LendingMarketElementVO> {
        return lendingMarketServices
            .filter {
                it.getNetwork() == network
            }.flatMap {
                it.getLendingMarkets()
            }.filter {
                it.token.address.lowercase() == token
            }.map { it.toVO() }
    }

    fun LendingMarketElement.toVO(): LendingMarketElementVO {
        return LendingMarketElementVO(
            name = name,
            protocol = protocol.toVO(),
            network = network.toVO(),
            token = LendingMarketElementToken(
                name = token.name,
                symbol = token.symbol,
                address = token.address,
                logo = token.logo
            ),
            rate = rate,
            poolType = poolType,
            marketSize = BigDecimal.valueOf(marketSize)
        )
    }
}