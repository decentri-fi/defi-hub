package io.defitrack

import io.defitrack.evm.contract.EvmContractInteractionCommand
import io.defitrack.evm.web3j.SimpleRateLimiter
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.exceptions.ClientConnectionException
import java.util.*

@RestController
@RequestMapping("/contract")
class EVMContractInteractionRestController(
    private val web3j: Web3j,
) {

    val simpleRateLimiter = SimpleRateLimiter(20.0)

    @PostMapping("/call")
    fun call(@RequestBody evmContractInteractionCommand: EvmContractInteractionCommand) =
        runBlocking {
            simpleRateLimiter.acquire()
            performCall(evmContractInteractionCommand)
        }

    suspend fun performCall(evmContractInteractionCommand: EvmContractInteractionCommand): Any {
        return with(evmContractInteractionCommand) {
            try {
                web3j.ethCall(
                    Transaction.createEthCallTransaction(
                        from,
                        contract,
                        function
                    ), DefaultBlockParameterName.PENDING
                )!!.send()!!
            } catch (ex: ClientConnectionException) {
                if (ex.message?.contains("429") == true) {
                    delay(1000L)
                    return performCall(evmContractInteractionCommand)
                } else {
                    throw ex
                }
            }
        }
    }
}