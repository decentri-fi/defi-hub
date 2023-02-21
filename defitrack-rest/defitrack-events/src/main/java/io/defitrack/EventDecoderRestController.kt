package io.defitrack

import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.EventDecoder
import io.defitrack.evm.contract.BlockchainGatewayProvider
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/decode")
class EventDecoderRestController(
    private val eventDecoders: List<EventDecoder>,
    private val gatewayProvider: BlockchainGatewayProvider,
) {

    @GetMapping("/{txId}", params = ["network"])
    fun decodeTransaction(
        @PathVariable("txId") txId: String,
        @RequestParam("network") network: Network
    ): List<DefiEvent> = runBlocking {
        val logs = gatewayProvider.getGateway(network).getLogs(txId)
        logs.flatMap {
            eventDecoders.map { decoder ->
                if (decoder.appliesTo(it)) {
                    decoder.extract(it, network)
                } else {
                    null
                }
            }
        }.filterNotNull()
    }
}