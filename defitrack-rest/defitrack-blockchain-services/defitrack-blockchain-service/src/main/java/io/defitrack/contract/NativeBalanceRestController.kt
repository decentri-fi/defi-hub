package io.defitrack.contract

import io.defitrack.evm.web3j.EvmGateway
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
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
    @Observed(name = "requests.get.balance.by-address")
    suspend fun getNativeBalance(@PathVariable("address") address: String): BigInteger {
        return evmGateway.ethGetBalance(address)
    }
}