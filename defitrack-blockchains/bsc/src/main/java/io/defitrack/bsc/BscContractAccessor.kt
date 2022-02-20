package io.defitrack.bsc

import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.abi.domain.AbiContractEvent
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.web3j.EvmGateway
import io.reactivex.Flowable
import org.springframework.stereotype.Component
import org.web3j.abi.EventEncoder
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.websocket.WebSocketClient
import org.web3j.protocol.websocket.WebSocketService
import java.net.URI
import java.util.*

@Component
class BscContractAccessor(abiDecoder: AbiDecoder, val bscGateway: BscGateway) :
    EvmContractAccessor(abiDecoder) {

    override fun getMulticallContract(): String {
       return  "0x41263cba59eb80dc200f3e2544eda4ed6a90e76c"
    }

    override fun getNetwork(): Network {
        return Network.BSC
    }

    override fun getGateway(): EvmGateway {
        return bscGateway
    }
}