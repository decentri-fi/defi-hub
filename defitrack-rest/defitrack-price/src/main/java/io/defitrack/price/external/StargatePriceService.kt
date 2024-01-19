package io.defitrack.price.external

import io.defitrack.domain.FungibleToken
import io.defitrack.domain.GetPriceCommand
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.port.input.PriceResource
import io.defitrack.protocol.stargate.contract.StargatePool
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class StargatePriceService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val priceResource: PriceResource
) : ExternalPriceService {

    val logger = LoggerFactory.getLogger(this::class.java)

    val tokens = listOf(
        "S*USDC",
        "S*USDT",
        "S*DAI",
        "S*FRAX",
        "S*MAI",
        "S*WOO",
        "S*sUSD",
        "S*LUSD"
    )

    override suspend fun appliesTo(token: FungibleToken): Boolean {
        return tokens.any {
            token.symbol == it
        }
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return emptyList()
    }

    override suspend fun getPrice(fungibleToken: FungibleToken): BigDecimal {
        return try {
            val contract = StargatePool(
                blockchainGatewayProvider.getGateway(fungibleToken.network.toNetwork()),
                fungibleToken.address
            )
            return priceResource.calculatePrice(
                GetPriceCommand(
                    address = contract.token(),
                    fungibleToken.network.toNetwork(),
                    BigDecimal.ONE
                )
            ).toBigDecimal()
        } catch (ex: Exception) {
            logger.error("Error while calculating price for ${fungibleToken.symbol}", ex)
            BigDecimal.ZERO
        }
    }
}
