package io.defitrack.rest

import arrow.core.None
import arrow.core.Option
import arrow.core.toOption
import io.defitrack.vo.TransactionVO
import io.defitrack.web3j.Web3JProxy
import io.github.reactivecircus.cache4k.Cache
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.core.methods.response.Log
import java.util.concurrent.Executors
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.days


@RestController
@RequestMapping("/tx")
class TransactionRestController(
    private val web3JProxy: Web3JProxy
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val txCache = Cache.Builder<String, Option<TransactionVO>>().expireAfterWrite(3.days).build()

    @GetMapping("/{txId}")
    suspend fun getTransaction(@PathVariable("txId") txId: String): TransactionVO? {
        return txCache.get(txId.lowercase()) {
            web3JProxy.getTransactionByHash(txId).transaction.getOrNull()?.let {
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
                ).toOption()
            } ?: None
        }.getOrNull()
    }

    val logCache = Cache.Builder<String, List<Log>>().expireAfterWrite(3.days).build()

    @GetMapping("/{txId}/logs")
    suspend fun getLogs(@PathVariable("txId") txId: String): List<Log> {
        return logCache.get(txId.lowercase()) {
            web3JProxy.getTransactionReceipt(txId).transactionReceipt.map {
                it.logs
            }.orElse(emptyList())
        }
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 3) //every 3 hours
    fun logCacheSizes() {
        logger.info("transaction log cache contains {} entries", logCache.asMap().size)
        logger.info("transaction cache contains {} entries", txCache.asMap().size)
    }
}