package io.defitrack

import io.defitrack.evm.web3j.EvmGateway
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigInteger

@RestController
@RequestMapping("/balance")
class NativeBalanceRestController(
    private val evmGateway: EvmGateway
) {
    @GetMapping("/{address}")
    suspend fun getNativeBalance(@PathVariable("address") address: String): BigInteger {
        return evmGateway.ethGetBalance(address)
    }
}