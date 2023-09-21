package io.defitrack.contract

import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import kotlinx.coroutines.delay
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigInteger

@RestController
@RequestMapping("/balance")
class NativeBalanceRestController(
    private val web3j: Web3j
) {
    @GetMapping("/{address}")
    @Timed("blockchain.balance.by-address")
    suspend fun getNativeBalance(@PathVariable("address") address: String): BigInteger {
        val send = web3j.ethGetBalance(address, DefaultBlockParameterName.PENDING).send()
        return if (send.hasError() && send.error.code == 429) {
            delay(1000)
            getNativeBalance(address)
        } else {
            send.balance
        }
    }
}