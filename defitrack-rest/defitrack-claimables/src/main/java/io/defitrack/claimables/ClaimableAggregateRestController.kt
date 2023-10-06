package io.defitrack.claimables

import arrow.fx.coroutines.parMap
import io.defitrack.claimable.UserClaimableVO
import io.defitrack.protocol.DefiPrimitive
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mapper.ProtocolVOMapper
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.web3j.crypto.WalletUtils.isValidAddress
import java.util.concurrent.Executors
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.measureTimedValue

@RestController
@Tag(name = "Claimables")
class ClaimableAggregateRestController(
    private val claimablesClient: ClaimablesClient,
    private val protocolVOMapper: ProtocolVOMapper,
    private val observationRegistry: ObservationRegistry
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{address}")
    @Operation(summary = "Get all claimables for a specific address")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Found Claimables",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = (ArraySchema(schema = Schema(implementation = UserClaimableVO::class)))
                    )
                ]
            )]
    )
    suspend fun aggregate(@PathVariable("address") address: String): List<UserClaimableVO> = coroutineScope {
        if (!isValidAddress(address)) {
            emptyList()
        } else {
            val observation = Observation.start("requests.get.claimables.aggregate", observationRegistry)
            val result = measureTimedValue {
                Protocol.entries.filter {
                    it.primitives.contains(DefiPrimitive.CLAIMABLES)
                }.parMap {
                    claimablesClient.getClaimables(address, protocolVOMapper.map(it))
                }.flatten()
            }

            logger.info("took ${result.duration.inWholeSeconds} seconds to aggregate ${result.value.size} claimables for $address")
            observation.stop()
            result.value
        }
    }

    val executor = Executors.newSingleThreadExecutor()

    @GetMapping("/{address}", params = ["sse"])
    @Hidden
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