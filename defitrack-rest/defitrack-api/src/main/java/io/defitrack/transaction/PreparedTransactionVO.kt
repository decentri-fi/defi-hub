package io.defitrack.transaction

import io.defitrack.network.NetworkVO

class PreparedTransactionVO(
    val network: NetworkVO,
    val data: String,
    val to: String,
    val from: String? = null
)