package io.defitrack.market.pooling.history

import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.market.pooling.domain.PoolingMarket
import org.web3j.protocol.core.methods.response.EthLog

data class HistoricEventExtractor(
    val addresses: () -> List<String>,
    val topic: String,
    val optionalTopics: (user: String) -> List<String?>,
    val toMarketEvent: suspend (logObject: EthLog.LogObject) -> DefiEvent?
)