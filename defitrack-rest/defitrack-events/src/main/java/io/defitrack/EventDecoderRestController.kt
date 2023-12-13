package io.defitrack

import arrow.core.Either
import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.events.native.NativeTransactionDecoder
import io.defitrack.evm.contract.BlockchainGatewayProvider
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/decode")
class EventDecoderRestController(
    private val eventDecoders: List<EventDecoder>,
    private val gatewayProvider: BlockchainGatewayProvider,
    private val nativeTransactionDecoder: NativeTransactionDecoder
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

        val logs = gatewayProvider.getGateway(network).getLogs(txId)

        if (logs.size > 300) {
            return emptyList()
        }

        return logs.take(maxLogs ?: 100).parMap {
            eventDecoders
                .filter {
                    (type == null || it.eventTypes().contains(type))
                }
                .parMapNotNull(concurrency = 8) { decoder ->
                    Either.catch {
                        decoder.toDefiEvent(it, network)
                    }.mapLeft {
                        logger.error("Error decoding event", it)
                    }.getOrNull()
                }
        }.flatten()
            .plus(
                nativeTransactionDecoder.extract(txId, network)
            ).filter {
                type == null || it.type == type
            }
    }
}