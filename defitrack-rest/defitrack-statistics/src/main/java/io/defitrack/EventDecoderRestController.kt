package io.defitrack

import io.defitrack.common.network.Network
import org.springframework.web.bind.annotation.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

@RestController
@RequestMapping("/decode")
class EventDecoderRestController(
) {

    @GetMapping("/{txId}", params = ["network"])
    fun decodeTransaction(
        @PathVariable("txId") txId: String,
        @RequestParam("network") network: Network
    ) {
        val web3j =
            Web3j.build(HttpService("https://speedy-nodes-nyc.moralis.io/defb1b75360993e48d0cffdb/ethereum/mainnet"))

        val tx = web3j.ethGetTransactionByHash(txId).send()
        println(tx)
    }
}