package io.defitrack.protocol.quickswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.defitrack.protocol.quickswap.staking.invest.DQuickStakingInvestmentPreparer
import org.springframework.stereotype.Service

@Service
class DQuickFarmingMarketProvider(
    private val quickswapService: QuickswapService,
    private val abiResource: ABIResource,
) : FarmingMarketProvider() {

    val dquickStakingABI by lazy {
        abiResource.getABI("quickswap/dquick.json")
    }

    val dquick by lazy {
        DQuickContract(
            getBlockchainGateway(),
            dquickStakingABI,
            quickswapService.getDQuickContract(),
        )
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val stakedToken = getToken(dquick.address).toFungibleToken()
        val quickToken = getToken("0x831753dd7087cac61ab5644b308642cc1c33dc13").toFungibleToken()

        return listOf(
            create(
                identifier = dquick.address.lowercase(),
                name = "Dragon's Lair",
                stakedToken = quickToken,
                rewardTokens = listOf(
                    stakedToken
                ),
                vaultType = "quickswap-dquick",
                balanceFetcher = PositionFetcher(
                    stakedToken.address,
                    { user -> dquick.balanceOfMethod(user) }
                ),
                investmentPreparer = DQuickStakingInvestmentPreparer(
                    getERC20Resource(), dquick
                ),
                farmType = FarmType.LIQUIDITY_MINING
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}