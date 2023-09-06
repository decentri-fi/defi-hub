package io.defitrack.ens.rest

import fi.decentri.ens.domain.ENSResult
import io.defitrack.ens.service.EnsNameService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ENSRestController(private val ensNameService: EnsNameService) {

    @GetMapping("/by-name/{ens}")
    suspend fun getAddressInformation(@PathVariable("ens") ens: String): ENSResult {
        val result = ensNameService.getEnsByName(ens)
        return ENSResult(
            ens, result
        )
    }

    @GetMapping("/by-address/{address}")
    suspend fun getMapping(@PathVariable("address") address: String): ENSResult {
        val result = ensNameService.getEnsByAddress(address)
        return ENSResult(
            result, address
        )
    }

    @GetMapping("/by-name/{name}/avatar")
    suspend fun getAvatar(@PathVariable name: String): Map<String, String> {
        val result = ensNameService.getAvatar(name)
        return if (result.isNotBlank()) {
            mapOf(
                "avatar" to result
            )
        } else {
            val ensByName = ensNameService.getEnsByName(name)
            if (ensByName.isNotBlank()) {
                mapOf(
                    "avatar" to "https://metadata.ens.domains/preview/${name}"
                )
            } else {
                emptyMap()
            }
        }
    }
}