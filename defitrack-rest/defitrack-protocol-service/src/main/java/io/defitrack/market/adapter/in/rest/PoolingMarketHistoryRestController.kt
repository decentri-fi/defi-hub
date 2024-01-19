package io.defitrack.market.adapter.`in`.rest

import io.defitrack.common.network.Network.Companion.fromString
import io.defitrack.market.adapter.`in`.resource.GetHistoryRequest
import io.defitrack.market.adapter.`in`.resource.MinimalPoolingMarketVO
import io.defitrack.market.adapter.`in`.resource.PoolingDefiEventVO
import io.defitrack.market.domain.GetHistoryCommand
import io.defitrack.market.port.`in`.PoolingHistory
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/{protocol}/pooling")
class PoolingMarketHistoryRestController(
    private val poolingHistory: PoolingHistory
) {

    @PostMapping("/history/{user}")
    fun getMarketEvents(
        @PathVariable("protocol") protocol: String,
        @PathVariable("user") user: String,
        @RequestBody getHistoryRequest: GetHistoryRequest
    ): List<PoolingDefiEventVO> = runBlocking {
        val network = fromString(getHistoryRequest.network) ?: throw IllegalArgumentException(
            "Invalid network ${getHistoryRequest.network}"
        )
        val proto = Protocol.Companion.fromString(protocol) ?: throw IllegalArgumentException(
            "Invalid protocol $protocol"
        )

        poolingHistory.getPoolingHistory(
            GetHistoryCommand(
                proto,
                network,
                user,
                getHistoryRequest.fromBlock,
                getHistoryRequest.toBlock,
                getHistoryRequest.markets
            )
        ).map {
            PoolingDefiEventVO(
                MinimalPoolingMarketVO(
                    it.poolingmarket.id,
                    it.poolingmarket.name
                ),
                it.event
            )
        }
    }
}