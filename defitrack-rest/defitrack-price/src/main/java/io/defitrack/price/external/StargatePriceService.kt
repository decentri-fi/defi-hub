package io.defitrack.price.external

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.contract.StargatePool
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
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

    override fun appliesTo(tokenInformationVO: TokenInformationVO): Boolean {
        return tokens.any {
            tokenInformationVO.symbol == it
        }
    }

    override suspend fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal {
        return try {
            val contract = StargatePool(
                blockchainGatewayProvider.getGateway(tokenInformationVO.network.toNetwork()),
                tokenInformationVO.address
            )
            return priceResource.calculatePrice(
                PriceRequest(
                    address = contract.token(),
                    tokenInformationVO.network.toNetwork(),
                    BigDecimal.ONE
                )
            ).toBigDecimal()
        } catch (ex: Exception) {
            logger.error("Error while calculating price for ${tokenInformationVO.symbol}", ex)
            BigDecimal.ZERO
        }
    }
}
