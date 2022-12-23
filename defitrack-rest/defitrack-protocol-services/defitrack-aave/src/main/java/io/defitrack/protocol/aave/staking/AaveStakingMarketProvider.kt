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

        val totalStakedAave = erc20Resource.getBalance(getNetwork(), aave, stAave)
        val ratio = totalStakedAave.toBigDecimal().dividePrecisely(stAaveContract.totalSupply().toBigDecimal())

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
                    { retVal ->
                        val userStAave = (retVal[0].value as BigInteger).toBigDecimal()
                        userStAave.times(ratio).toBigInteger()
                    }
                ),
                farmType = FarmType.STAKING,
                claimableRewardFetcher = ClaimableRewardFetcher(
                    address = stAave,
                    function = { user ->
                        stAaveContract.getTotalRewardFunction(user)
                    },
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            getNetwork().toVO(),
                            stAaveContract.getClaimRewardsFunction(user),
                            stAave
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