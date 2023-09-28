package io.defitrack

import io.defitrack.evm.contract.EvmContractInteractionCommand
import io.defitrack.evm.web3j.EvmGateway
import io.micrometer.observation.annotation.Observed
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.core.methods.response.EthCall

@RestController
@RequestMapping("/contract")
class EVMContractInteractionRestController(
    private val evmGateway: EvmGateway,
) {

    @PostMapping("/call")
    suspend fun call(@RequestBody evmContractInteractionCommand: EvmContractInteractionCommand): EthCall {
        return evmGateway.call(evmContractInteractionCommand)
    }
}