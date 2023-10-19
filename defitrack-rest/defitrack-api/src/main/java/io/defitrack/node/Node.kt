package io.defitrack.node

import io.defitrack.protocol.Company
import io.defitrack.protocol.CompanyVO

data class Node(
    val name: String,
    val companies: List<Company>
)