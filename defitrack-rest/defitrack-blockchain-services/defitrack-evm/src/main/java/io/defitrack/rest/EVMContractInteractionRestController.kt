package io.defitrack.rest

import io.defitrack.web3j.Web3JProxy
import io.defitrack.evm.EvmContractInteractionCommand
import io.github.reactivecircus.cache4k.Cache
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.core.methods.response.EthCall
import java.util.*
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds

@RestController
@RequestMapping("/contract")
class EVMContractInteractionRestController(
    private val web3JProxy: Web3JProxy,
) {

    val mutex = Mutex()

    private val logger = LoggerFactory.getLogger(this::class.java)

    val outputs = Cache.Builder<String, EthCall>().expireAfterWrite(10.seconds).build()
    val inputs = Cache.Builder<String, EvmContractInteractionCommand>().expireAfterWrite(10.seconds).build()

    @PostMapping("/call")
    suspend fun call(@RequestBody evmContractInteractionCommand: EvmContractInteractionCommand): EthCall {
        return web3JProxy.call(evmContractInteractionCommand)
    }

    @PostMapping("/call/async")
    suspend fun async(@RequestBody evmContractInteractionCommand: EvmContractInteractionCommand): EthCall {
        return coroutineScope {
            val asy = async {
                val uuid = UUID.randomUUID().toString()
                inputs.put(uuid, evmContractInteractionCommand)
                collect(uuid).first()
            }

            withTimeout(5000) {
                asy.await()
            }
        }
    }

    suspend fun collect(uuid: String) = channelFlow {
        launch {
            while (true) {
                val ethCall = outputs.get(uuid)
                if (ethCall != null) {
                    send(ethCall)
                    invalidateOutput(uuid)
                    break
                }
                delay(100)
            }
        }
    }

    suspend fun invalidateOutput(uuid: String) {
        mutex.withLock {
            outputs.invalidate(uuid)
        }
    }

    suspend fun invalidateInput(uuid: String) {
        mutex.withLock {
            inputs.invalidate(uuid)
        }
    }

    @PostConstruct
    fun run() {
        Executors.newSingleThreadExecutor().submit {
            runBlocking {
                while (true) {
                    try {
                        val requests = getInputs()
                        if (requests.size > 1) {
                            val multicallResults = web3JProxy.call(requests.map { it.value })
                            multicallResults.mapIndexed { index, ethCall ->
                                val uuid = requests[index].key as String
                                outputs.put(uuid, ethCall)
                                invalidateInput(uuid)
                            }
                        } else {
                            try {
                                requests.forEach {
                                    val uuid = it.key

                                    val value = web3JProxy.call(it.value)
                                    outputs.put(uuid as String, value)
                                    invalidateInput(uuid)
                                }
                            } catch (ex: Exception) {
                                logger.error("unable to iterate requests", ex)
                            }
                        }

                        delay(100)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        logger.error("Error in multicall", ex)
                    }
                }
            }
        }
    }

    private suspend fun getInputs(): List<MutableMap.MutableEntry<Any?, EvmContractInteractionCommand>> = mutex.withLock{
        return try {
            HashMap(inputs.asMap()).entries.toMutableList()
        } catch (ex: Exception) {
            logger.error("unable to fetch inputs", ex)
            emptyList()
        }
    }
}