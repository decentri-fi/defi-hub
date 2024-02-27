package io.defitrack.protocol.quickswap.staking

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
class OldDQuickFarmingMarketProvider(
    private val quickswapService: QuickswapService,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = with(getBlockchainGateway()) {
            DQuickContract(
                quickswapService.getOldDQuickContractAddress(),
            )
        }
        val stakedToken = getToken(contract.address)
        val quickToken = getToken("0x831753dd7087cac61ab5644b308642cc1c33dc13")

        return listOf(
            create(
                identifier = contract.address.lowercase(),
                name = "Dragon's Lair (Old)",
                stakedToken = quickToken,
                rewardTokens = listOf(
                    stakedToken
                ),
                positionFetcher = PositionFetcher(
                    stakedToken.asERC20Contract(getBlockchainGateway())::balanceOfFunction
                ),
                investmentPreparer = DQuickStakingInvestmentPreparer(
                    getERC20Resource(), contract
                ),
                deprecated = true,
                exitPositionPreparer = prepareExit {
                    contract.exitFunction(it.amount)
                },
                type = "quickswap.farming"
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