package io.defitrack

import io.defitrack.evm.contract.EvmContractInteractionCommand
import io.defitrack.evm.web3j.SimpleRateLimiter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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
class EVMContractInteractionRestController(
    private val web3j: Web3j,
) {

    val simpleRateLimiter = SimpleRateLimiter(20.0)

    @PostMapping("/call")
    suspend fun call(@RequestBody evmContractInteractionCommand: EvmContractInteractionCommand): EthCall {
        simpleRateLimiter.acquire()
        return performCall(evmContractInteractionCommand)
    }

    suspend fun performCall(evmContractInteractionCommand: EvmContractInteractionCommand): EthCall {
        return withContext(Dispatchers.IO) {
            with(evmContractInteractionCommand) {
                try {
                    val result = web3j.ethCall(
                        Transaction.createEthCallTransaction(
                            from,
                            contract,
                            function
                        ), DefaultBlockParameterName.PENDING
                    ).send()

                    if (result.hasError() && result.error.code == 429) {
                        delay(1000L)
                        performCall(evmContractInteractionCommand)
                    } else {
                        result
                    }
                } catch (ex: ClientConnectionException) {
                    if (ex.message?.contains("429") == true) {
                        delay(1000L)
                        return@with performCall(evmContractInteractionCommand)
                    } else {
                        throw ex
                    }
                }
            }
        }
    }
}