package io.defitrack.claimables

import arrow.fx.coroutines.parMap
import io.defitrack.PageUtils
import io.defitrack.claimable.vo.ClaimableMarketVO
import io.defitrack.claimable.vo.UserClaimableVO
import io.defitrack.exception.ExceptionResult
import io.defitrack.protocol.DefiPrimitive
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mapper.ProtocolVOMapper
import io.github.reactivecircus.cache4k.Cache
import io.micrometer.observation.Observation.start
import io.micrometer.observation.ObservationRegistry
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.web3j.crypto.WalletUtils.isValidAddress
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.hours
import kotlin.time.measureTimedValue

@RestController
@Tag(
    name = "Claimables",
    description = "Claimables API, enabling us to easily fetch all types of claimables from different protocols"
)
class ClaimableAggregateRestController(
    private val claimablesClient: ClaimablesClient,
    private val protocolVOMapper: ProtocolVOMapper,
    private val observationRegistry: ObservationRegistry
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val cache = Cache.Builder<String, List<ClaimableMarketVO>>().expireAfterWrite(1.hours).build()

    @GetMapping
    @Operation(summary = "Get all claimables markets")
    suspend fun getPagedMarkets(pageable: Pageable) = coroutineScope {
        val result = cache.get("all") {
            Protocol.entries.filter {
                it.primitives.contains(DefiPrimitive.CLAIMABLES)
            }.parMap {
                claimablesClient.getClaimableMarkets(protocolVOMapper.map(it))
            }.flatten()
        }

        PageUtils.createPageFromList(
            result, pageable
        )
    }

    @GetMapping("/{address}")
    @Operation(
        summary = "Get all claimables for a specific address",
        parameters = [
            Parameter(
                name = "include",
                description = "What protocols to include. If empty, all protocols will be included. Both slug and name can be used. Ex: 'gmx' or 'GMX'",
                required = false,
                array = ArraySchema(schema = Schema(implementation = String::class))
            ),
            Parameter(
                name = "exclude",
                description = "What protocols to exclude. If empty, no protocols will be excluded. Both slug and name can be used. Ex: 'gmx' or 'GMX'",
                required = false,
                array = ArraySchema(schema = Schema(implementation = String::class))
            )
        ]
    )
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
            ),
            ApiResponse(
                responseCode = "403",
                description = "Validation exception",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ExceptionResult::class),
                        array = (ArraySchema(schema = Schema(implementation = UserClaimableVO::class)))
                    )
                ]
            )]
    )
    suspend fun aggregate(
        @PathVariable("address") address: String,
        @RequestParam("include", required = false, defaultValue = "") includes: List<String> = emptyList(),
        @RequestParam("exclude", required = false, defaultValue = "") excludes: List<String> = emptyList()
    ): ResponseEntity<Any> {
        if (includes.isNotEmpty() && excludes.isNotEmpty())
            return ResponseEntity(
                ExceptionResult("Cannot include and exclude at the same time"),
                BAD_REQUEST
            )

        if (!isValidAddress(address))
            return ResponseEntity(
                ExceptionResult("Invalid address"),
                BAD_REQUEST
            )

        val observation = start("requests.get.claimables.aggregate", observationRegistry)
        val result = measureTimedValue {
            val protocols = Protocol.entries.filter {
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
            protocols.parMap {
                claimablesClient.getClaimables(address, protocolVOMapper.map(it))
            }.flatten()
        }

        logger.info("took ${result.duration.inWholeSeconds} seconds to aggregate ${result.value.size} claimables for $address")
        observation.stop()
        return ResponseEntity.ok(result.value)
    }

    val executor = Executors.newSingleThreadExecutor()

    @GetMapping("/{address}", params = ["sse"])
    @Hidden
    fun getAggregateAsSSE(
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
                            claimablesClient.getClaimables(address, protocolVOMapper.map(it)).forEach {
                                emitter.send(it)
                            }
                        }
                    }.joinAll()
                    emitter.complete()
                    observation.stop();
                }
            }
        }
        httpServletResponse.addHeader("X-Accel-Buffering", "no")
        return emitter
    }
}