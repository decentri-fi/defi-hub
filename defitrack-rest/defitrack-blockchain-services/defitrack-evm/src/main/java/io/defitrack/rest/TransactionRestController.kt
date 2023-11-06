package io.defitrack.rest

import io.defitrack.vo.TransactionVO
import io.defitrack.web3j.Web3JProxy
import kotlinx.coroutines.coroutineScope
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.Log
import kotlin.jvm.optionals.getOrNull

@RestController
@RequestMapping("/tx")
class TransactionRestController(
    private val web3JProxy: Web3JProxy
) {
    @GetMapping("/{txId}")
    suspend fun getTransaction(@PathVariable("txId") txId: String): TransactionVO? {
        return web3JProxy.getTransactionByHash(txId).transaction.getOrNull()?.let {
            val possibleSpam = web3JProxy.getTransactionReceipt(txId).transactionReceipt.map {
                it.logs.size > 400
            }.orElse(false)

            TransactionVO(
                hash = it.hash,
                blockNumber = it.blockNumber,
                from = it.from,
                to = it.to,
                time = web3JProxy.getBlockByHash(it.blockHash)!!.block.timestamp.longValueExact(),
                value = it.value,
                possibleSpam = possibleSpam
            )
        }
    }

    @GetMapping("/{txId}/logs")
    suspend fun getLogs(@PathVariable("txId") txId: String): List<Log> {
        return web3JProxy.getTransactionReceipt(txId).transactionReceipt.map {
            it.logs
        }.orElse(emptyList())
    }
}