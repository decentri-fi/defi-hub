package io.defitrack.protocol.pendle

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.int256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EventUtils.extract
import io.defitrack.evm.contract.DeprecatedEvmContract
import io.defitrack.evm.contract.EvmContract
import org.springframework.cglib.core.Block
import org.web3j.abi.EventEncoder
import org.web3j.abi.datatypes.Event
import java.math.BigInteger

context(BlockchainGateway)
class PendleMarketFactoryContract( address: String
) : EvmContract(address) {

    val createNewMarketEvent = Event(
        "CreateNewMarket",
        listOf(
            TypeUtils.address(true), //market
            TypeUtils.address(true), //PT
            int256(false), //scalarRoot
            int256(false), //initialAnchor
            uint256(false), //InFeeRateRot
        ),
    )

    suspend fun getMarkets(fromBlock: String = "18669498"): List<Market> {
        val logs = getEventsAsEthLog(
            GetEventLogsCommand(
                addresses = listOf(this.address),
                topic = EventEncoder.encode(createNewMarketEvent),
                fromBlock = BigInteger(fromBlock, 10),
            )
        )

        return logs.map {
            Market(
                createNewMarketEvent.extract(it, true, 0) as String,
                createNewMarketEvent.extract(it, true, 1) as String
            )
        }
    }

    data class Market(
        val market: String,
        val pt: String
    )
}