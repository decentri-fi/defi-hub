package io.defitrack.protocol.quickswap.staking

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.defitrack.protocol.quickswap.staking.invest.DQuickStakingInvestmentPreparer
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class DQuickFarmingMarketProvider(
    private val quickswapService: QuickswapService,
) : FarmingMarketProvider() {

    val dquickStakingABI by lazy {
        runBlocking {
            getAbi("quickswap/dquick.json")
        }
    }

    val oldDQuick by lazy {
        runBlocking {
            DQuickContract(
                getBlockchainGateway(),
                dquickStakingABI,
                quickswapService.getDQuickContract(),
            )
        }
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val stakedToken = getToken(oldDQuick.address).toFungibleToken()
        val quickToken = getToken("0x831753dd7087cac61ab5644b308642cc1c33dc13").toFungibleToken()

        return listOf(
            create(
                identifier = oldDQuick.address.lowercase(),
                name = "Dragon's Lair",
                stakedToken = quickToken,
                rewardTokens = listOf(
                    stakedToken
                ),
                vaultType = "quickswap-dquick",
                balanceFetcher = PositionFetcher(
                    stakedToken.address,
                    { user -> ERC20Contract.balanceOfFunction(user) }
                ),
                investmentPreparer = DQuickStakingInvestmentPreparer(
                    getERC20Resource(), oldDQuick
                ),
                farmType = ContractType.YIELD_OPTIMIZING_AUTOCOMPOUNDER,
                exitPositionPreparer = prepareExit {
                    PreparedExit(
                        oldDQuick.exitFunction(it.amount),
                        oldDQuick.address,
                    )
                }
            )
        )
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}