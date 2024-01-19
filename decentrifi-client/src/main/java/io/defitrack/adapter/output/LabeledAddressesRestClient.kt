package io.defitrack.adapter.output

import io.defitrack.adapter.output.resource.LabeledAddressVO
import io.defitrack.labeledaddress.domain.LabeledAddress
import io.defitrack.labeledaddress.port.out.LabeledAddresses
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class LabeledAddressesRestClient(
    @Value("\${labeledAddressesResourceLocation:http://labeled-addresses.default.svc.cluster.local:8080}") val labeledAddressesResourceLocation: String,
    private val client: HttpClient
) : LabeledAddresses {

    val cache = Cache.Builder<String, LabeledAddress>()
        .build()

    override suspend fun getLabel(address: String): LabeledAddress {
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