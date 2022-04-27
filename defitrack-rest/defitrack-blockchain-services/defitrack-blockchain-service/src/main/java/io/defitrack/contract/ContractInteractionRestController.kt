package io.defitrack.contract

import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import io.defitrack.evm.contract.ContractInteractionCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthCall

@RestController
@RequestMapping("/contract")
class ContractInteractionRestController(
    private val web3j: Web3j
) {
    @PostMapping("/call")
    fun call(@RequestBody contractInteractionCommand: ContractInteractionCommand): EthCall =
        runBlocking(Dispatchers.IO) {
            retry(limitAttempts(10) + binaryExponentialBackoff(base = 10L, max = 60000L)) {
                with(contractInteractionCommand) {
                    web3j.ethCall(
                        Transaction.createEthCallTransaction(
                            from,
                            contract,
                            function
                        ), DefaultBlockParameterName.LATEST
                    ).send()
                }
            }
        }
}