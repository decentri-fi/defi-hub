package io.defitrack.ens.rest

import io.defitrack.ens.domain.ENSResult
import io.defitrack.ens.service.EnsNameService
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(
    name = "ENS",
    description = "ENS API, enabling us to easily fetch ens information."
)
class ENSRestController(
    private val ensNameService: EnsNameService,
    private val observationRegistry: ObservationRegistry
) {

    @GetMapping("/by-name/{ens}")
    @Operation(summary = "Get ENS information by ens-name")
    suspend fun getAddressInformation(@PathVariable("ens") ens: String): ENSResult {
        val observation = Observation.start("ens_by_name", observationRegistry)

        return observation.openScope().use {
            val result = ensNameService.getEnsByName(ens)

            ENSResult(
                ens, result, if (result.isNotBlank()) {
                    ensNameService.getExpires(ens).toLong()
                } else 0
            )
        }.also { observation.stop() }
    }

    @GetMapping("/by-address/{address}")
    @Operation(summary = "Get ENS information by address")
    suspend fun getMapping(@PathVariable("address") address: String): ENSResult {
        val observation = Observation.start("ens_by_address", observationRegistry)

        return observation.openScope().use {
            val result = ensNameService.getEnsByAddress(address)

            ENSResult(
                result, address, if (result.isNotBlank()) {
                    ensNameService.getExpires(result).toLong()
                } else {
                    0
                }
            )
        }.also { observation.stop() }
    }

    @GetMapping("/by-name/{name}/avatar")
    @Operation(summary = "Get avatar information by ens-name")
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