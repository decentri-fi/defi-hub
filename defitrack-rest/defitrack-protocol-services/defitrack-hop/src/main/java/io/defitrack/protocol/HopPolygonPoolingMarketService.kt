package io.defitrack.protocol

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.pool.domain.PoolingToken
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.contract.HopLpTokenContract
import io.defitrack.protocol.contract.HopSwapContract
import io.defitrack.protocol.staking.TokenType
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class HopPolygonPoolingMarketService(
    private val hopService: HopService,
    private val hopAPRService: HopAPRService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiResource: ABIResource,
    private val priceResource: PriceResource,
    private val erC20Resource: ERC20Resource
) : PoolingMarketService() {


    override fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return hopService.getLps(getNetwork()).map { hopLpToken ->

            val contract = HopLpTokenContract(
                evmContractAccessor = polygonContractAccessor,
                abiResource.getABI("hop/SaddleToken.json"),
                hopLpToken.lpToken
            )

            val swapContract = HopSwapContract(
                evmContractAccessor = polygonContractAccessor,
                abiResource.getABI("hop/Swap.json"),
                contract.swap
            )

            val htoken = erC20Resource.getERC20(getNetwork(), hopLpToken.hToken)
            val canonical = erC20Resource.getERC20(getNetwork(), hopLpToken.canonicalToken)

            val marketSize = getPrice(canonical.address, contract, swapContract).toBigDecimal()
            PoolingMarketElement(
                id = "hop-polygon-${hopLpToken.canonicalToken}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = hopLpToken.lpToken,
                name = contract.name,
                token = listOf(
                    PoolingToken(
                        name = htoken.name,
                        symbol = htoken.symbol,
                        address = htoken.address
                    ),
                    PoolingToken(
                        name = canonical.name,
                        symbol = canonical.symbol,
                        address = canonical.address
                    )
                ),
                marketSize = marketSize,
                apr = hopAPRService.getAPR(
                    canonical.symbol,
                    canonical.address,
                    canonical.decimals,
                    getNetwork(),
                    marketSize
                )
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