package io.defitrack.claimables

import io.defitrack.claimable.UserClaimableVO
import io.defitrack.protocol.DefiPrimitive
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mapper.ProtocolVOMapper
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.web3j.crypto.WalletUtils.isValidAddress
import java.util.concurrent.Executors
import kotlin.time.measureTimedValue

@RestController
class ClaimableAggregateRestController(
    private val claimablesClient: ClaimablesClient,
    private val protocolVOMapper: ProtocolVOMapper,
    private val observationRegistry: ObservationRegistry
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{address}")
    suspend fun aggregate(@PathVariable("address") address: String): List<UserClaimableVO> = coroutineScope {
        if (!isValidAddress(address)) {
            emptyList()
        } else {
            val observation = Observation.start("requests.get.claimables.aggregate", observationRegistry)
            val result = measureTimedValue {
                //todo: fetch protocols from api gw (cached), so we don't have to redeploy this service every time we add a new protocol
                Protocol.entries.filter {
                    it.primitives.contains(DefiPrimitive.CLAIMABLES)
                }.map {
                    async {
                        claimablesClient.getClaimables(address, protocolVOMapper.map(it))
                    }
                }.awaitAll().flatten()
            }

            logger.info("took ${result.duration.inWholeSeconds} seconds to aggregate ${result.value.size} claimables for $address")
            observation.stop()
            result.value
        }
    }

    val executor = Executors.newSingleThreadExecutor()

    @GetMapping("/{address}", params = ["sse"])
    fun getAggregateAsSSE(
        @PathVariable("address") address: String,
        httpServletResponse: HttpServletResponse
    ): SseEmitter {
        val observation = Observation.start("requests.get.claimables.aggregate.sse", observationRegistry)
        val emitter = SseEmitter()
        executor.submit {
            runBlocking {
                launch {
                    Protocol.entries.filter {
                        it.primitives.contains(DefiPrimitive.CLAIMABLES)
                    }.map {
                        launch {
                            claimablesClient.getClaimables(address, protocolVOMapper.map(it)).forEach {
                                emitter.send(it)
                            }
                        }
                    }.joinAll()
                    emitter.complete()
                    observation.stop();
                }
                httpServletResponse.addHeader("X-Accel-Buffering", "no")
            }
        }
        return emitter
    }
}