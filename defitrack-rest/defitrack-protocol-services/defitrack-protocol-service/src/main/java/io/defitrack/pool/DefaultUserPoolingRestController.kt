package io.defitrack.pool

import io.defitrack.network.toVO
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.pool.vo.PoolingElementVO
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.toVO
import io.defitrack.price.PriceResource
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pooling")
class DefaultUserPoolingRestController(
    private val userPoolingServices: List<UserPoolingService>,
    private val priceResource: PriceResource
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{userId}/positions")
    fun getUserPoolings(@PathVariable("userId") address: String): List<PoolingElementVO> {
        return userPoolingServices.flatMap {
            try {
                it.userPoolings(address)
            } catch (ex: Exception) {
                logger.error("Something went wrong trying to fetch the user poolings: ${ex.message}")
                emptyList()
            }
        }.map {
            it.toVO()
        }
    }

    fun PoolingElement.toVO(): PoolingElementVO {
        return PoolingElementVO(
            lpAddress = lpAddress,
            amount = amount.toDouble(),
            name = name,
            dollarValue = priceResource.calculatePrice(
                PriceRequest(
                    lpAddress,
                    network,
                    amount,
                    tokenType
                )
            ),
            network = network.toVO(),
            symbol = symbol,
            protocol = protocol.toVO(),
            id = id
        )
    }
}