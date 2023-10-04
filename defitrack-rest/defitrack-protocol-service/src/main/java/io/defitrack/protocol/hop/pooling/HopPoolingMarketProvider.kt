package io.defitrack.protocol.hop.pooling

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.hop.HopService
import io.defitrack.protocol.hop.apr.HopAPRService
import io.defitrack.protocol.hop.contract.HopLpTokenContract
import io.defitrack.protocol.hop.contract.HopSwapContract
import io.defitrack.protocol.hop.domain.HopLpToken
import io.defitrack.token.TokenType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

abstract class HopPoolingMarketProvider(
    private val hopService: HopService,
    private val hopAPRService: HopAPRService
) : PoolingMarketProvider() {

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        hopService.getLps(getNetwork()).forEach { hopLpToken ->
            launch {
                throttled {
                    toPoolingMarketElement(getBlockchainGateway(), hopLpToken)?.let {
                        send(it)
                    }
                }
            }
        }
    }

    private suspend fun toPoolingMarketElement(
        gateway: BlockchainGateway,
        hopLpToken: HopLpToken
    ): PoolingMarket? {
        return try {
            val contract = HopLpTokenContract(
                blockchainGateway = gateway,
                hopLpToken.lpToken
            )

            val swapContract = HopSwapContract(
                blockchainGateway = gateway,
                contract.swap()
            )

            val htoken = getToken(hopLpToken.hToken)
            val canonical = getToken(hopLpToken.canonicalToken)

            val marketSize = getPrice(canonical.address, contract, swapContract).toBigDecimal()
            create(
                identifier = hopLpToken.canonicalToken,
                address = hopLpToken.lpToken,
                symbol = htoken.symbol + "-" + canonical.symbol,
                name = contract.name(),
                tokens = listOf(
                    htoken.toFungibleToken(),
                    canonical.toFungibleToken()
                ),
                marketSize = refreshable(marketSize) {
                    getPrice(canonical.address, contract, swapContract).toBigDecimal()
                },
                apr = hopAPRService.getAPR(
                    canonical.symbol,
                    canonical.address,
                    canonical.decimals,
                    getNetwork(),
                    marketSize
                ),
                positionFetcher = defaultPositionFetcher(hopLpToken.lpToken),
                totalSupply = refreshable {
                    contract.totalSupply().asEth(contract.decimals())
                }
            )
        } catch (ex: Exception) {
            logger.error("unable to generate market for lptoken {}", hopLpToken, ex)
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


    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }
}