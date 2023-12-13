package io.defitrack.labeledaddresses

import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class LabeledAddressesResource(
    @Value("\${labeledAddressesResourceLocation:http://labeled-addresses.default.svc.cluster.local:8080}") val labeledAddressesResourceLocation: String,
    private val client: HttpClient
) {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    val cache = Cache.Builder<String, LabeledAddress>()
        .build()

    suspend fun getLabel(address: String): LabeledAddress {
        return try {
            cache.get(address) {
                val result: LabeledAddressVO = client.get(labeledAddressesResourceLocation + "/" + address) {
                    this.header("Content-Type", "application/json")
                }.body()
                LabeledAddress(
                    address = result.address,
                    label = result.tag
                )
            }
        } catch (ex: Exception) {
            LabeledAddress(
                address = address,
                label = null
            )
        }
    }
}