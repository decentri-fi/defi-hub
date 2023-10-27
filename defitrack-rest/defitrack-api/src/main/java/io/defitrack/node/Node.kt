package io.defitrack.node

import io.defitrack.protocol.Company

data class Node(
    val name: String,
    val companies: List<Company>
)