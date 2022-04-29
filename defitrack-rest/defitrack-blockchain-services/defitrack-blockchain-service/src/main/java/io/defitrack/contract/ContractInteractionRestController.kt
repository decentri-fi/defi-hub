package io.defitrack.contract

import io.defitrack.evm.contract.ContractInteractionCommand
import io.defitrack.evm.web3j.SimpleRateLimiter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.exceptions.ClientConnectionException

@RestController
@RequestMapping("/contract")
class ContractInteractionRestController(
    private val web3j: Web3j
) {

    val simpleRateLimiter = SimpleRateLimiter(10.0)

    @PostMapping("/call")
    fun call(@RequestBody contractInteractionCommand: ContractInteractionCommand): EthCall =
        runBlocking(Dispatchers.IO) {
            simpleRateLimiter.acquire()
            performCall(contractInteractionCommand)
        }

    suspend fun performCall(contractInteractionCommand: ContractInteractionCommand): EthCall {
        return with(contractInteractionCommand) {
            try {
                web3j.ethCall(
                    Transaction.createEthCallTransaction(
                        from,
                        contract,
                        function
                    ), DefaultBlockParameterName.LATEST
                ).send()
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
}