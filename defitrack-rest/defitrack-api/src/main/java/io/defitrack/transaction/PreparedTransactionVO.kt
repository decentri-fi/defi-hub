package io.defitrack.transaction

import io.defitrack.networkinfo.NetworkInformation

class PreparedTransactionVO(
    val network: NetworkInformation,
    val data: String,
    val to: String,
    val from: String? = null
)