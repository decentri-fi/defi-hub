package io.defitrack.fantom.config

import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.abi.domain.AbiContractEvent
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.web3j.EvmGateway
import io.reactivex.Flowable
import org.springframework.stereotype.Component
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
import java.util.concurrent.CompletableFuture

@Component
class FantomContractAccessor(abiDecoder: AbiDecoder, val fantomGateway: FantomGateway) :
    EvmContractAccessor(abiDecoder) {
    override fun getMulticallContract(): String {
        return "0x2d5408f2287bf9f9b05404794459a846651d0a59"
    }

    override fun getNetwork(): Network {
        return Network.FANTOM
    }

    override fun getGateway(): EvmGateway {
        return fantomGateway
    }
}