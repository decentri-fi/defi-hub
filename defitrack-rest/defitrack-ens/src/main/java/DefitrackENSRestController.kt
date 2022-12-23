package io.defitrack

import io.defitrack.domain.ENSResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class DefitrackENSRestController {

    @GetMapping("/by-name/{ens}")
    fun getAddressInformation(@PathVariable("ens") ens: String): ENSResult {
        return ENSResult(
            "", ""
        )
    }

    @GetMapping("/by-address/{address}")
    fun getMapping(@PathVariable("address") address: String): ENSResult {
        return ENSResult(
            "", ""
        )
    }
}