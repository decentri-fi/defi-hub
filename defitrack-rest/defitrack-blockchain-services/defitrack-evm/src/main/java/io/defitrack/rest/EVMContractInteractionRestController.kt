package io.defitrack.rest

import io.defitrack.evm.EvmContractInteractionCommand
import io.defitrack.web3j.Web3JProxy
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
import java.util.concurrent.ConcurrentLinkedQueue
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
    val inputQueue = ConcurrentLinkedQueue<CallToExecute>()

    data class CallToExecute(val uuid: String, val evmContractInteractionCommand: EvmContractInteractionCommand)

    @PostMapping("/call")
    suspend fun call(@RequestBody evmContractInteractionCommand: EvmContractInteractionCommand): EthCall {
        return web3JProxy.call(evmContractInteractionCommand)
    }

    @PostMapping("/call/async")
    suspend fun async(@RequestBody evmContractInteractionCommand: EvmContractInteractionCommand): EthCall {
        return coroutineScope {
            val asy = async {
                val uuid = UUID.randomUUID().toString()
                inputQueue.add(CallToExecute(uuid, evmContractInteractionCommand))
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

    @PostConstruct
    fun run() {
        Executors.newSingleThreadExecutor().submit {
            runBlocking {
                while (true) {
                    if (inputQueue.any()) {
                        logger.info("inputqueue contains ${inputQueue.size} elements")
                    }
                    try {
                        val requests = getInputs()
                        if (requests.size > 1) {
                            val multicallResults = web3JProxy.call(requests.map { it.evmContractInteractionCommand })
                            multicallResults.mapIndexed { index, ethCall ->
                                val uuid = requests[index].uuid
                                outputs.put(uuid, ethCall)
                            }
                        } else {
                            try {
                                requests.forEach {
                                    val value = web3JProxy.call(it.evmContractInteractionCommand)
                                    outputs.put(it.uuid, value)
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

    private suspend fun getInputs(): List<CallToExecute> {
        return inputQueue.map {
               it.also {
                   inputQueue.remove(it)
               }
        }
    }
}