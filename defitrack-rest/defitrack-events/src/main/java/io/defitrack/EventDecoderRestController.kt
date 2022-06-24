package io.defitrack

import io.defitrack.common.network.Network
import io.defitrack.events.DefiEvent
import io.defitrack.events.EventDecoder
import io.defitrack.evm.contract.BlockchainGatewayProvider
import org.springframework.web.bind.annotation.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

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
    ): List<DefiEvent> {
        val logs = gatewayProvider.getGateway(network).getLogs(txId)
        return logs.flatMap {
            eventDecoders.map { decoder ->
                if (decoder.appliesTo(it)) {
                    decoder.extract(it)
                } else {
                    null
                }
            }
        }.filterNotNull()
    }
}