package io.defitrack

import io.defitrack.starknet.config.StarknetInteractionCommand
import io.defitrack.evm.web3j.SimpleRateLimiter
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jService
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.exceptions.ClientConnectionException
import java.util.*

@RestController
@RequestMapping("/starknet-contract")
class StarknetContractInteractionRestController(
    web3j: Web3j,
    private val web3jService: Web3jService
) {

    val simpleRateLimiter = SimpleRateLimiter(20.0)

    @PostMapping("/call")
    fun callStarknet(@RequestBody contractInteractionCommand: StarknetInteractionCommand): Any =
        runBlocking {
            simpleRateLimiter.acquire()
            performCall(contractInteractionCommand)
        }

    suspend fun performCall(contractInteractionCommand: StarknetInteractionCommand): StarknetCallResponse {
        return with(contractInteractionCommand) {
            try {
                starknetCall(
                    contractAddress,
                    entryPointSelector,
                    calldata
                ).send()!!
            } catch (ex: ClientConnectionException) {
                if (ex.message?.contains("429") == true) {
                    delay(1000L)
                    return performCall(contractInteractionCommand)
                } else {
                    throw ex
                }
            }
        }
    }

    fun starknetCall(
        contractAddress: String,
        entryPointSelector: String,
        calldata: List<String>
    ): Request<Any, StarknetCallResponse> {
        val call = StarknetCall(
            contract_address = contractAddress,
            entry_point_selector = entryPointSelector,
            calldata = calldata
        )
        return Request("starknet_call", listOf(call, "latest"), web3jService, StarknetCallResponse::class.java)
    }

    class StarknetCall(
        val contract_address: String,
        val entry_point_selector: String,
        val calldata: List<String>
    )

    class StarknetCallResponse : Response<List<String>>() {
    }
}