package io.defitrack

import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.evm.contract.BlockchainGatewayProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/decode")
class EventDecoderRestController(
    private val eventDecoders: List<EventDecoder>,
    private val gatewayProvider: BlockchainGatewayProvider,
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{txId}", params = ["network"])
    suspend fun decodeTransaction(
        @PathVariable("txId") txId: String,
        @RequestParam("network") networkAsString: String,
        @RequestParam("type", required = false) type: DefiEventType? = null,
        @RequestParam("max-logs", required = false) maxLogs: Int? = 100
    ): List<DefiEvent> {
        val network =
            Network.fromString(networkAsString) ?: throw IllegalArgumentException("Invalid network $networkAsString")

        val sema = Semaphore(16)

        val logs = gatewayProvider.getGateway(network).getLogs(txId)

        if (logs.size > 300) {
            return emptyList()
        }

        return coroutineScope {
            logs.take(maxLogs ?: 100).map {
                async {
                    eventDecoders
                        .filter {
                            (type == null || it.eventTypes().contains(type))
                        }
                        .map { decoder ->
                            try {
                                sema.withPermit {
                                    if (decoder.appliesTo(it, network)) {
                                        decoder.extract(it, network)
                                    } else {
                                        null
                                    }
                                }
                            } catch (ex: Exception) {
                                logger.debug("Error decoding event for tx $txId")
                                null
                            }
                        }
                }
            }.awaitAll().flatten().filterNotNull().filter {
                type == null || it.type == type
            }
        }
    }
}