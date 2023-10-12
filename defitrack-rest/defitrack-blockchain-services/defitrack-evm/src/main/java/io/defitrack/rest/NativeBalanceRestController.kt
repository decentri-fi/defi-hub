package io.defitrack.rest

import io.defitrack.web3j.Web3JProxy
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigInteger

@RestController
@RequestMapping("/balance")
class NativeBalanceRestController(
    private val web3JProxy: Web3JProxy
) {
    @GetMapping("/{address}")
    suspend fun getNativeBalance(@PathVariable("address") address: String): BigInteger {
        return web3JProxy.ethGetBalance(address)
    }
}