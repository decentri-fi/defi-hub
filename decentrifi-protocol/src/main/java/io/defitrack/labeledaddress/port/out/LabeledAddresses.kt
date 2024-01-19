package io.defitrack.labeledaddress.port.out

import io.defitrack.labeledaddress.domain.LabeledAddress

interface LabeledAddresses {
    suspend fun getLabel(address: String): LabeledAddress
}