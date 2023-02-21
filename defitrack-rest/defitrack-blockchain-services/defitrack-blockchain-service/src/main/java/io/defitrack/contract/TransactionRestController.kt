package io.defitrack.contract

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.Log
import java.util.*
import kotlin.jvm.optionals.getOrNull

@RestController
@RequestMapping("/tx")
class TransactionRestController(
    private val web3j: Web3j
) {

    @OptIn(ExperimentalStdlibApi::class)
    @GetMapping("/{txId}")
    fun getTransaction(@PathVariable("txId") txId: String): TransactionVO? {
        return web3j.ethGetTransactionByHash(txId).send().transaction.map {
            TransactionVO(
                hash = it.hash,
                blockNumber = it.blockNumber,
                from = it.from,
                to = it.to,
                time = web3j.ethGetBlockByHash(it.blockHash, false).send().block.timestamp.longValueExact(),
                value = it.value
            )
        }.getOrNull()
    }

    @GetMapping("/{txId}/logs")
    fun getLogs(@PathVariable("txId") txId: String): List<Log> {
        return web3j.ethGetTransactionReceipt(txId).send().transactionReceipt.map {
            it.logs
        }.orElse(emptyList())
    }
}