package io.defitrack

import io.defitrack.common.network.Network
import io.defitrack.events.DefiEvent
import io.defitrack.events.EventDecoder
import org.springframework.web.bind.annotation.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

@RestController
@RequestMapping("/decode")
class EventDecoderRestController(
    private val eventDecoders: List<EventDecoder>
) {

    @GetMapping("/{txId}", params = ["network"])
    fun decodeTransaction(
        @PathVariable("txId") txId: String,
        @RequestParam("network") network: Network
    ): List<DefiEvent> {
        val web3j =
            Web3j.build(HttpService("https://eth-mainnet.alchemyapi.io/v2/lGuOps3bb7y1wl2U7LCeyaPoM4Er4HZn"))

        return web3j.ethGetTransactionReceipt(txId).send().transactionReceipt.get().logs.flatMap {
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