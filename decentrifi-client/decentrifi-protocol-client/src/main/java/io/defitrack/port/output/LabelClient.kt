package io.defitrack.port.output

import io.defitrack.adapter.output.domain.label.LabeledAddressDTO

interface LabelClient {

    suspend fun getLabel(address: String): LabeledAddressDTO
}
