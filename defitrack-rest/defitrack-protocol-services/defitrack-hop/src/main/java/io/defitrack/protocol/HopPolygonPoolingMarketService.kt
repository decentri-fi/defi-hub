package io.defitrack.protocol

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.contract.HopLpTokenContract
import io.defitrack.protocol.contract.HopSwapContract
import io.defitrack.token.ERC20Resource
import io.defitrack.token.MarketSizeService
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class HopPolygonPoolingMarketService(
    private val hopService: HopService,
    private val hopAPRService: HopAPRService,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val abiResource: ABIResource,
    private val priceResource: PriceResource,
    private val erC20Resource: ERC20Resource,
) : PoolingMarketService() {


    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        val gateway = contractAccessorGateway.getGateway(getNetwork())

        return hopService.getLps(getNetwork()).map { hopLpToken ->

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
        }
    }

    private fun getPrice(
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