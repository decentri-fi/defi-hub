package io.defitrack.token

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.domain.FungibleToken
import io.defitrack.domain.GetPriceCommand
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.port.input.ERC20Resource
import io.defitrack.port.input.PriceResource
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class MarketSizeService(
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    suspend fun getMarketSizeInUSD(tokens: List<FungibleToken>, location: String, network: Network): BigDecimal {
        return tokens.sumOf {
            getMarketSize(it, location, network).usdAmount
        }
    }

    suspend fun getMarketSize(
        token: FungibleToken,
        location: String,
        network: Network
    ): MarketSize {
        val balance = if (token.address == "0x0") {
            val gateway = blockchainGatewayProvider.getGateway(network)
            gateway.getNativeBalance(location).times(BigDecimal.TEN.pow(18)).toBigInteger()
        } else {
            erC20Resource.getBalance(network, token.address, location)
        }

        return MarketSize(
            tokenAmount = balance,
            usdAmount = priceResource.calculatePrice(
                GetPriceCommand(
                    address = token.address,
                    network = network,
                    amount = balance.asEth(token.decimals),
                )
            ).toBigDecimal()
        )
    }

    data class MarketSize(
        val tokenAmount: BigInteger,
        val usdAmount: BigDecimal
    )
}