package io.defitrack.protocol.quickswap.staking

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.defitrack.protocol.quickswap.staking.invest.DQuickStakingInvestmentPreparer
import org.springframework.stereotype.Component


@Component
@ConditionalOnCompany(Company.QUICKSWAP)
@ConditionalOnNetwork(Network.POLYGON)
class DQuickFarmingMarketProvider(
    private val quickswapService: QuickswapService,
) : FarmingMarketProvider() {

    private val QUICK = "0x831753dd7087cac61ab5644b308642cc1c33dc13"


    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = with(getBlockchainGateway()) { DQuickContract(quickswapService.getDQuickContract()) }

        val stakedToken = getToken(contract.address)
        val quickToken = getToken(QUICK)

        return create(
            identifier = contract.address.lowercase(),
            name = "Dragon's Lair",
            stakedToken = quickToken,
            rewardToken = stakedToken,
            positionFetcher = PositionFetcher(contract::balanceOfFunction),
            investmentPreparer = DQuickStakingInvestmentPreparer(
                getERC20Resource(), balanceResource, contract
            ),
            type = "quickswap.dragons-lair",
            exitPositionPreparer = prepareExit {
                contract.exitFunction(it.amount)
            }
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}