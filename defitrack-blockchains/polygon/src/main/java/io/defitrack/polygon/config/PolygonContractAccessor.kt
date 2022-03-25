package io.defitrack.polygon.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.ContractInteractionCommand
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.web3j.EvmGateway
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.web3j.protocol.core.methods.response.EthCall

@Component
class PolygonContractAccessor(
    abiDecoder: AbiDecoder,
    private val polygonGateway: PolygonGateway,
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.polygon.endpoint:http://defitrack-polygon:8080}") private val polygonMicroserviceEndpoint: String,
) :
    EvmContractAccessor(abiDecoder) {

    override fun call(
        from: String?,
        contract: String,
        encodedFunction: String
    ): EthCall = runBlocking(Dispatchers.IO) {
       httpClient.post("$polygonMicroserviceEndpoint/contract/call") {
            contentType(ContentType.Application.Json)
            this.body =
                ContractInteractionCommand(
                    from = from,
                    contract = contract,
                    function = encodedFunction
                )
        }
    }

    override fun getMulticallContract(): String {
        return "0x11ce4B23bD875D7F5C6a31084f55fDe1e9A87507"
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    override fun getGateway(): EvmGateway {
        return polygonGateway
    }
}