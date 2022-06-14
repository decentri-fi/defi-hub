package io.defitrack.protocol

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.contract.HopLpTokenContract
import io.defitrack.protocol.contract.HopSwapContract
import io.defitrack.protocol.domain.HopLpToken
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class HopPolygonPoolingMarketService(
    private val hopService: HopService,
    private val hopAPRService: HopAPRService,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val abiResource: ABIResource,
    private val priceResource: PriceResource,
    private val erC20Resource: ERC20Resource,
) : PoolingMarketService() {


    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> = coroutineScope {
        val gateway = blockchainGatewayProvider.getGateway(getNetwork())

        hopService.getLps(getNetwork()).map { hopLpToken ->
            async(Dispatchers.IO.limitedParallelism(10)) {
                toPoolingMarketElement(gateway, hopLpToken)
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun toPoolingMarketElement(
        gateway: BlockchainGateway,
        hopLpToken: HopLpToken
    ): PoolingMarketElement? {
        return try {
            val contract = HopLpTokenContract(
                blockchainGateway = gateway,
                abiResource.getABI("hop/SaddleToken.json"),
                hopLpToken.lpToken
            )

            val swapContract = HopSwapContract(
                blockchainGateway = gateway,
                abiResource.getABI("hop/Swap.json"),
                contract.swap
            )

            val htoken = erC20Resource.getTokenInformation(getNetwork(), hopLpToken.hToken)
            val canonical = erC20Resource.getTokenInformation(getNetwork(), hopLpToken.canonicalToken)

            val marketSize = getPrice(canonical.address, contract, swapContract).toBigDecimal()
            PoolingMarketElement(
                id = "hop-polygon-${hopLpToken.canonicalToken}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = hopLpToken.lpToken,
                symbol = htoken.symbol + "-" + canonical.symbol,
                name = contract.name,
                tokens = listOf(
                    htoken.toFungibleToken(),
                    canonical.toFungibleToken()
                ),
                marketSize = marketSize,
                apr = hopAPRService.getAPR(
                    canonical.symbol,
                    canonical.address,
                    canonical.decimals,
                    getNetwork(),
                    marketSize
                ),
                tokenType = TokenType.HOP
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    private suspend fun getPrice(
        canonicalTokenAddress: String,
        contract: HopLpTokenContract,
        swapContract: HopSwapContract
    ): Double {

        val tokenAmount = contract.totalSupply.toBigDecimal().times(
            swapContract.virtualPrice.toBigDecimal()
        ).divide(BigDecimal.TEN.pow(36))

        return priceResource.calculatePrice(
            PriceRequest(
                address = canonicalTokenAddress,
                network = getNetwork(),
                amount = tokenAmount,
                TokenType.SINGLE
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}