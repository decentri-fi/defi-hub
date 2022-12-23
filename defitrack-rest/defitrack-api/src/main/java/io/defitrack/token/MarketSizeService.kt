package io.defitrack.token

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class MarketSizeService(
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    suspend fun getMarketSize(tokens: List<FungibleToken>, location: String, network: Network): BigDecimal {
        return tokens.sumOf {
            getMarketSize(it, location, network)
        }
    }

    suspend fun getMarketSize(
        token: FungibleToken,
        location: String,
        network: Network
    ): BigDecimal {
       val balance =  if (token.address == "0x0") {
           val gateway = blockchainGatewayProvider.getGateway(network)
           gateway.getNativeBalance(location)
        } else {
           erC20Resource.getBalance(network, token.address, location).asEth(token.decimals)
        }

        return priceResource.calculatePrice(
            PriceRequest(
                address = token.address,
                network = network,
                amount = balance,
                type = token.type
            )
        ).toBigDecimal()
    }
}