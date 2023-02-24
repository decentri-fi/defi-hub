package io.defitrack

import io.defitrack.domain.ENSResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class DefitrackENSRestController(private val ensNameService: EnsNameService) {

    @GetMapping("/by-name/{ens}")
    fun getAddressInformation(@PathVariable("ens") ens: String): ENSResult {
        val result = ensNameService.getEnsByName(ens)
        return ENSResult(
            ens, result
        )
    }

    @GetMapping("/by-address/{address}")
    fun getMapping(@PathVariable("address") address: String): ENSResult {
        val result = ensNameService.getEnsByAddress(address)
        return ENSResult(
            result, address
        )
    }

    @GetMapping("/by-name/{name}/avatar")
    fun getAvatar(@PathVariable name: String): Map<String, String> {
        val result = ensNameService.getAvatar(name)
        return if (result.isNotBlank()) {
            mapOf(
                "avatar" to result
            )
        } else {
            val result = ensNameService.getEnsByName(name)
            if (result.isNotBlank()) {
                mapOf(
                    "avatar" to "https://metadata.ens.domains/preview/${name}"
                )
            } else {
                emptyMap()
            }
        }
    }
}