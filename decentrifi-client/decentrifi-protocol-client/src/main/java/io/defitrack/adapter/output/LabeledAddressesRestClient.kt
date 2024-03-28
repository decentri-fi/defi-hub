package io.defitrack.adapter.output

import io.defitrack.adapter.output.domain.label.LabeledAddressDTO
import io.defitrack.port.output.LabelClient
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class LabeledAddressesRestClient(
    @Value("\${labeledAddressesResourceLocation:http://labeled-addresses.default.svc.cluster.local:8080}") val labeledAddressesResourceLocation: String,
    private val client: HttpClient
) : LabelClient {

    val cache = Cache.Builder<String, LabeledAddressDTO>()
        .build()

   override suspend fun getLabel(address: String): LabeledAddressDTO = withContext(Dispatchers.IO){
        try {
            cache.get(address) {
                client.get("$labeledAddressesResourceLocation/$address") {
                    this.header("Content-Type", "application/json")
                }.body()
            }
        } catch (ex: Exception) {
            LabeledAddressDTO(
                address = address,
                label = ""
            )
        }
    }
}