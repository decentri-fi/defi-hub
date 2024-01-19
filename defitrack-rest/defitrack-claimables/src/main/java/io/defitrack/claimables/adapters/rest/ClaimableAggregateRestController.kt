package io.defitrack.claimables.adapters.rest

import arrow.fx.coroutines.parMap
import io.defitrack.PageUtils
import io.defitrack.claimables.ports.inputs.ClaimableAggregateRestControllerDocumentation
import io.defitrack.claimables.ports.outputs.ClaimablesClient
import io.defitrack.exception.ExceptionResult
import io.defitrack.protocol.DefiPrimitive
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mapper.ProtocolVOMapper
import io.micrometer.observation.Observation.start
import io.micrometer.observation.ObservationRegistry
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
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
) : ClaimableAggregateRestControllerDocumentation {

    private val logger = LoggerFactory.getLogger(this::class.java)


    override suspend fun getPagedMarkets(
        @RequestParam("include", required = false, defaultValue = "") includes: List<String>,
        @RequestParam("exclude", required = false, defaultValue = "") excludes: List<String>,
        pageable: Pageable,
    ): ResponseEntity<Any> {

        if (includes.isNotEmpty() && excludes.isNotEmpty())
            return ResponseEntity(
                ExceptionResult("Cannot include and exclude protocols at the same time"),
                BAD_REQUEST
            )

        val result = filteredProtocols(includes, excludes).parMap {
            claimablesClient.getClaimableMarkets(it)
        }.flatten()

        return ResponseEntity.ok(
            PageUtils.createPageFromList(
                result, pageable
            )
        )
    }

    override suspend fun aggregate(
        @PathVariable("address") address: String,
        @RequestParam("include", required = false, defaultValue = "") includes: List<String>,
        @RequestParam("exclude", required = false, defaultValue = "") excludes: List<String>
    ): ResponseEntity<Any> {
        if (includes.isNotEmpty() && excludes.isNotEmpty())
            return ResponseEntity(
                ExceptionResult("Cannot include and exclude protocols at the same time"),
                BAD_REQUEST
            )

        if (!isValidAddress(address))
            return ResponseEntity(
                ExceptionResult("Invalid address"),
                BAD_REQUEST
            )

        val observation = start("requests.get.claimables.aggregate", observationRegistry)
        val result = measureTimedValue {
            claimablesClient.getClaimables(address, filteredProtocols(includes, excludes))
        }.also {
            if (it.duration.inWholeSeconds > 3) {
                logger.info("took ${it.duration.inWholeSeconds} seconds to aggregate ${it.value.size} claimables for $address")
            }
        }

        observation.stop()
        return ResponseEntity.ok(result.value)
    }

    private fun filteredProtocols(
        includes: List<String>,
        excludes: List<String>
    ) = Protocol.entries.filter {
        it.primitives.contains(DefiPrimitive.CLAIMABLES)
    }.filter {
        if (includes.isNotEmpty()) {
            includes.contains(it.name) || includes.contains(it.slug)
        } else true
    }.filter {
        if (excludes.isNotEmpty()) {
            !excludes.contains(it.name) && !excludes.contains(it.slug)
        } else true
    }

    val executor = Executors.newSingleThreadExecutor()


    override fun getAggregateAsSSE(
        @PathVariable("address") address: String,
        httpServletResponse: HttpServletResponse
    ): SseEmitter {
        val observation = start("requests.get.claimables.aggregate.sse", observationRegistry)
        val emitter = SseEmitter()
        executor.submit {
            runBlocking {
                launch {
                    Protocol.entries.filter {
                        it.primitives.contains(DefiPrimitive.CLAIMABLES)
                    }.map {
                        launch {
                            claimablesClient.getClaimables(address, listOf(it)).forEach {
                                emitter.send(it)
                            }
                        }
                    }.joinAll()
                    emitter.complete()
                    observation.stop()
                }
            }
        }
        httpServletResponse.addHeader("X-Accel-Buffering", "no")
        return emitter
    }
}