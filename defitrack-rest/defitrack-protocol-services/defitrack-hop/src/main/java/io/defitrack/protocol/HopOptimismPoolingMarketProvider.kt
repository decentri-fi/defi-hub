package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.contract.HopLpTokenContract
import io.defitrack.protocol.contract.HopSwapContract
import io.defitrack.protocol.domain.HopLpToken
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class HopOptimismPoolingMarketProvider(
    private val hopService: HopService,
    private val hopAPRService: HopAPRService,
) : PoolingMarketProvider() {


    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        hopService.getLps(getNetwork()).map { hopLpToken ->
            async {
                toPoolingMarketElement(getBlockchainGateway(), hopLpToken)
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun toPoolingMarketElement(
        gateway: BlockchainGateway,
        hopLpToken: HopLpToken
    ): PoolingMarket? {
        return try {
            val contract = HopLpTokenContract(
                blockchainGateway = gateway,
                getAbi("hop/SaddleToken.json"),
                hopLpToken.lpToken
            )

            val swapContract = HopSwapContract(
                blockchainGateway = gateway,
                getAbi("hop/Swap.json"),
                contract.swap()
            )

            val htoken = getToken(hopLpToken.hToken)
            val canonical = getToken(hopLpToken.canonicalToken)

            val marketSize = getPrice(canonical.address, contract, swapContract).toBigDecimal()
            PoolingMarket(
                id = "hop-optimism-${hopLpToken.canonicalToken}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = hopLpToken.lpToken,
                symbol = htoken.symbol + "-" + canonical.symbol,
                name = contract.name(),
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
                tokenType = TokenType.HOP,
                positionFetcher = defaultPositionFetcher(hopLpToken.lpToken),
                totalSupply = contract.totalSupply()
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

        val tokenAmount = contract.totalSupply().toBigDecimal().times(
            swapContract.virtualPrice().toBigDecimal()
        ).divide(BigDecimal.TEN.pow(36))

        return getPriceResource().calculatePrice(
            PriceRequest(
                address = canonicalTokenAddress,
                network = getNetwork(),
                amount = tokenAmount,
                TokenType.SINGLE
            )
        )
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}