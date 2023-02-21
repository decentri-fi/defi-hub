package io.defitrack.transactions

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionRestController {

    @GetMapping("/{address}/transactions")
    fun getTransations(): List<TransactionVO> {
        return emptyList()
    }
}