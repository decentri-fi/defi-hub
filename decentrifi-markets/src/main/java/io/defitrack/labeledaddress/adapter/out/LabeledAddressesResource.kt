package io.defitrack.labeledaddress.adapter.out

import io.defitrack.labeledaddress.domain.LabeledAddress
import io.defitrack.labeledaddress.port.out.LabeledAddresses
import org.springframework.stereotype.Component

@Component
class LabeledAddressesResource(
    private val labeledAddresses: LabeledAddresses
) {
    suspend fun getLabel(address: String): LabeledAddress {
        return labeledAddresses.getLabel(address)
    }
}