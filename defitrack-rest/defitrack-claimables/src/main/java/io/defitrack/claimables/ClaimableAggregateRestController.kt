package io.defitrack.claimables

import io.defitrack.claimable.ClaimableVO
import io.defitrack.protocol.DefiPrimitive
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mapper.ProtocolVOMapper
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.web3j.crypto.WalletUtils.isValidAddress
import kotlin.time.measureTimedValue

@RestController
class ClaimableAggregateRestController(
    private val claimablesClient: ClaimablesClient,
    private val protocolVOMapper: ProtocolVOMapper
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{address}")
    suspend fun aggregate(@PathVariable("address") address: String): List<ClaimableVO> = coroutineScope {
        if (!isValidAddress(address)) {
            emptyList()
        } else {
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
            result.value
        }
    }

    @GetMapping("/{address}", params = ["sse"])
    fun getAggregateAsSSE(@PathVariable("address") address: String): ResponseEntity<ResponseBodyEmitter> {
        return runBlocking {
            val emitter = SseEmitter()

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
            }
            ResponseEntity.ok(emitter)
        }
    }
}