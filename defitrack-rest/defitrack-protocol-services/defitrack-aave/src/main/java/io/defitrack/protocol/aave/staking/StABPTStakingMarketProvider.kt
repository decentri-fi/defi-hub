package io.defitrack.protocol.aave.staking

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.MarketSizeService
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class StABPTStakingMarketProvider(
    private val erC20Resource: ERC20Resource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val marketSizeService: MarketSizeService,
    private val erc20Resource: ERC20Resource
) : FarmingMarketProvider() {

    private val stABPT = "0xa1116930326d21fb917d5a27f1e9943a9595fb47"
    private val aave = "0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9"
    private val abpt = "0x41a08648c3766f9f9d85598ff102a08f4ef84f84"
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val aaveToken = erC20Resource.getTokenInformation(getNetwork(), aave)
        val abptToken = erC20Resource.getTokenInformation(getNetwork(), abpt)

        val stakingContract = StakedAaveContract(
            blockchainGatewayProvider.getGateway(getNetwork()),
            stABPT
        )

        val totalStakedAbpt = erc20Resource.getBalance(getNetwork(), abpt, stABPT)
        val ratio = totalStakedAbpt.toBigDecimal().dividePrecisely(stakingContract.totalSupply().toBigDecimal())

        return listOf(
            create(
                name = "stABPT",
                identifier = "stABPT",
                stakedToken = abptToken.toFungibleToken(),
                rewardTokens = listOf(
                    aaveToken.toFungibleToken()
                ),
                vaultType = "stABPT",
                marketSize = marketSizeService.getMarketSize(
                    abptToken.toFungibleToken(), stABPT, getNetwork()
                ),
                apr = null,
                balanceFetcher = PositionFetcher(
                    stABPT,
                    { user ->
                        erC20Resource.balanceOfFunction(
                            stABPT, user, getNetwork()
                        )
                    },
                    { retVal ->
                        val userStAave = (retVal[0].value as BigInteger).toBigDecimal()
                        userStAave.times(ratio).toBigInteger()
                    }
                ),
                farmType = FarmType.STAKING,
                claimableRewardFetcher = ClaimableRewardFetcher(
                    address = stABPT,
                    function = { user ->
                        stakingContract.getTotalRewardFunction(user)
                    },
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            getNetwork().toVO(),
                            stakingContract.getClaimRewardsFunction(user),
                            stABPT
                        )
                    }
                )
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