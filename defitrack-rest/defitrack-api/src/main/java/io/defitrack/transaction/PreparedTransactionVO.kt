package io.defitrack.transaction

import io.defitrack.network.NetworkInformationVO

class PreparedTransactionVO(
    val network: NetworkInformationVO,
    val data: String,
    val to: String,
    val from: String? = null
)
