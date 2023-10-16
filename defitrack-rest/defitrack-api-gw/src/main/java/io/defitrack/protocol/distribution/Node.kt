package io.defitrack.protocol.distribution

import io.defitrack.protocol.Company

data class Node(
    val name: String,
    val companies: List<Company>
)