package io.defitrack.contract

import io.defitrack.evm.web3j.EvmGateway
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigInteger

@RestController
@RequestMapping("/api/balance")
class NativeBalanceRestController(private val evmGateway: EvmGateway) {

    @GetMapping("/{address}")
    fun getNativeBalance(@PathVariable("address") address: String): BigInteger {
        return evmGateway.web3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
    }
}