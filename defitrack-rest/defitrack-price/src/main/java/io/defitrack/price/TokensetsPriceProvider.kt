package io.defitrack.price

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.set.PolygonSetProvider
import io.defitrack.protocol.set.SetTokenContract
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class TokensetsPriceProvider(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val polygonSetProvider: PolygonSetProvider,
    private val priceResource: PriceResource,
    private val erC20Resource: ERC20Resource,
) : ExternalPriceService {

    val tokenMap by lazy {
        runBlocking {
            mapOf(
                Network.POLYGON to
                        polygonSetProvider.getSets().map {
                            SetTokenContract(
                                blockchainGatewayProvider.getGateway(Network.POLYGON), it
                            )
                        }
            )
        }
    }

    override suspend fun getPrice(network: Network, tokenInformationVO: TokenInformationVO): BigDecimal {

        val contract = tokenMap[network]?.firstOrNull {
            it.address.lowercase() == tokenInformationVO.address.lowercase()
        } ?: return BigDecimal.ZERO

        val positions = contract.getPositions()

        val price = positions.sumOf {
            val token = erC20Resource.getTokenInformation(network, it.token)

            priceResource.calculatePrice(
                PriceRequest(
                    it.token,
                    network,
                    it.amount.dividePrecisely(BigDecimal.TEN.pow(token.decimals)),
                    token.type
                )
            )
        }.toBigDecimal()

        return if (price <= BigDecimal.ZERO) {
            BigDecimal.ZERO
        } else price
    }

    override fun appliesTo(network: Network, tokenInformationVO: TokenInformationVO): Boolean {
        return tokenMap[network]?.any { set ->
            set.address.lowercase() == tokenInformationVO.address.lowercase()
        } ?: false
    }
}