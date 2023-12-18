package io.defitrack.market.pooling

import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network.Companion.fromString
import io.defitrack.event.DefiEvent
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.Network

@RestController
@RequestMapping("/{protocol}/pooling")
class PoolingMarketHistoryRestController(
    private val poolingHistoryAggregator: PoolingHistoryAggregator
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/history/{user}")
    suspend fun getMarketEvents(
        @PathVariable("protocol") protocol: String,
        @PathVariable("user") user: String,
        @RequestParam("network") networkAsString: String
    ): List<DefiEvent> {
        val network = fromString(networkAsString) ?: throw IllegalArgumentException(
            "Invalid network $networkAsString"
        )
        val proto = Protocol.Companion.fromString(protocol) ?: throw IllegalArgumentException(
            "Invalid protocol $protocol"
        )

        return poolingHistoryAggregator.getPoolingHistory(proto, network, user)
    }
}