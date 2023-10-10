package io.defitrack.protocol.quickswap.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.defitrack.protocol.quickswap.staking.invest.DQuickStakingInvestmentPreparer
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.QUICKSWAP)
class DQuickFarmingMarketProvider(
    private val quickswapService: QuickswapService,
) : FarmingMarketProvider() {

    val oldDQuick = lazyAsync {
        DQuickContract(
            getBlockchainGateway(),
            quickswapService.getDQuickContract(),
        )
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = oldDQuick.await()
        val stakedToken = getToken(contract.address).toFungibleToken()
        val quickToken = getToken("0x831753dd7087cac61ab5644b308642cc1c33dc13").toFungibleToken()

        return listOf(
            create(
                identifier = contract.address.lowercase(),
                name = "Dragon's Lair",
                stakedToken = quickToken,
                rewardTokens = listOf(
                    stakedToken
                ),
                balanceFetcher = PositionFetcher(
                    stakedToken.address,
                    { user -> ERC20Contract.balanceOfFunction(user) }
                ),
                investmentPreparer = DQuickStakingInvestmentPreparer(
                    getERC20Resource(), contract
                ),
                exitPositionPreparer = prepareExit {
                    PreparedExit(
                        contract.exitFunction(it.amount),
                        contract.address,
                    )
                }
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