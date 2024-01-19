package io.defitrack.ens.infrastructure.adapter.`in`.rest

import io.defitrack.ens.adapter.rest.vo.ENSResultVO
import io.defitrack.ens.application.input.ENSUseCase
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
    private val ens: ENSUseCase,
    private val observationRegistry: ObservationRegistry
) {

    @GetMapping("/by-name/{ens}")
    @Operation(summary = "Get ENS information by ens-name")
    suspend fun getAddressInformation(@PathVariable("ens") ens: String): ENSResultVO {
        val observation = Observation.start("ens_by_name", observationRegistry)

        return observation.openScope().use {
            val result = this.ens.getEnsByName(ens)

            ENSResultVO(
                ens, result, if (result.isNotBlank()) {
                    this.ens.getExpires(ens).toLong()
                } else 0
            )
        }.also { observation.stop() }
    }

    @GetMapping("/by-address/{address}")
    @Operation(summary = "Get ENS information by address")
    suspend fun getMapping(@PathVariable("address") address: String): ENSResultVO {
        val observation = Observation.start("ens_by_address", observationRegistry)

        return observation.openScope().use {
            val result = ens.getEnsByAddress(address)

            ENSResultVO(
                result, address, if (result.isNotBlank()) {
                    ens.getExpires(result).toLong()
                } else {
                    0
                }
            )
        }.also { observation.stop() }
    }

    @GetMapping("/by-name/{name}/avatar")
    @Operation(summary = "Get avatar information by ens-name")
    suspend fun getAvatar(@PathVariable name: String): Map<String, String> {
        val result = ens.getAvatar(name)
        return if (result.isNotBlank()) {
            mapOf(
                "avatar" to result
            )
        } else {
            val ensByName = ens.getEnsByName(name)
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