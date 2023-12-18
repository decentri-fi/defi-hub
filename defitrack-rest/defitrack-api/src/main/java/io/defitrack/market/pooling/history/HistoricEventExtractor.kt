package io.defitrack.market.pooling.history

import io.defitrack.event.DefiEvent
import io.defitrack.evm.contract.BlockchainGateway
import org.web3j.protocol.core.methods.response.EthLog

data class HistoricEventExtractor(
    val addresses: () -> List<String>,
    val topic: String,
    val optionalTopics: (user: String) -> List<String?>,
    val toMarketEvent: suspend (logObject: EthLog.LogObject, transactionVO: BlockchainGateway.TransactionVO) -> DefiEvent?
)