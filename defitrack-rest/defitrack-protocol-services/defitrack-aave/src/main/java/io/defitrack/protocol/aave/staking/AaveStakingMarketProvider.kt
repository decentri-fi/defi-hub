package io.defitrack.protocol.aave.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.MarketSizeService
import org.springframework.stereotype.Component

@Component
class AaveStakingMarketProvider(
    private val erC20Resource: ERC20Resource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val marketSizeService: MarketSizeService,
    private val erc20Resource: ERC20Resource
) : FarmingMarketProvider() {

    private val stAave = "0x4da27a545c0c5b758a6ba100e3a049001de870f5"
    private val aave = "0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val aaveToken = erC20Resource.getTokenInformation(getNetwork(), aave)

        val stAaveContract = StakedAaveContract(
            blockchainGatewayProvider.getGateway(getNetwork()),
            stAave
        )

        val totalStakedSushi = erc20Resource.getBalance(getNetwork(), aave, stAave)

        val ratio = totalStakedSushi.toBigDecimal().dividePrecisely(stAaveContract.totalSupply().toBigDecimal())



        return listOf(
            create(
                name = "stAave",
                identifier = "stAave",
                stakedToken = aaveToken.toFungibleToken(),
                rewardTokens = listOf(
                    aaveToken.toFungibleToken()
                ),
                vaultType = "stAave",
                marketSize = marketSizeService.getMarketSize(
                    aaveToken.toFungibleToken(), stAave, getNetwork()
                ),
                apr = null,
                balanceFetcher = PositionFetcher(
                    stAave,
                    { user ->
                        erC20Resource.balanceOfFunction(
                            stAave, user, getNetwork()
                        )
                    },
                ),
                farmType = FarmType.STAKING
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.AAVE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}