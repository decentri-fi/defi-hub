package io.defitrack.ens.rest

import com.newrelic.api.agent.Trace
import io.defitrack.ens.domain.ENSResult
import io.defitrack.ens.service.EnsNameService
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ENSRestController(private val ensNameService: EnsNameService) {

    @GetMapping("/by-name/{ens}")
    @Trace(metricName = "controller.ens.by-name")
    fun getAddressInformation(@PathVariable("ens") ens: String): ENSResult = runBlocking {
        val result = ensNameService.getEnsByName(ens)

        ENSResult(
            ens, result, if (result.isNotBlank()) {
                ensNameService.getExpires(ens).toLong()
            } else 0
        )
    }

    @GetMapping("/by-address/{address}")
    @Trace(metricName = "controller.ens.by-address")
    fun getMapping(@PathVariable("address") address: String): ENSResult = runBlocking {
        val result = ensNameService.getEnsByAddress(address)

       ENSResult(
            if(result.isNotBlank()) result else address, address, if (result.isNotBlank()) {
                ensNameService.getExpires(result).toLong()
            } else {
                0
            }
        )
    }

    @GetMapping("/by-name/{name}/avatar")
    @Trace(metricName = "controller.ens.by-name.avatar")
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