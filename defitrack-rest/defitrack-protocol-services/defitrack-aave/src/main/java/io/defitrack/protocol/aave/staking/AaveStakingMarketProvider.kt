package io.defitrack.protocol.aave.staking

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.Position
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class AaveStakingMarketProvider : FarmingMarketProvider() {

    private val stAave = "0x4da27a545c0c5b758a6ba100e3a049001de870f5"
    private val aave = "0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val aaveToken = getToken(aave)

        val stAaveContract = StakedAaveContract(
            getBlockchainGateway(),
            stAave
        )

        val totalStakedAave = getERC20Resource().getBalance(getNetwork(), aave, stAave)
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
                balanceFetcher = PositionFetcher(
                    stAave,
                    { user ->
                        balanceOfFunction(user)
                    },
                    { retVal ->
                        val userStAave = (retVal[0].value as BigInteger)
                        Position(
                            userStAave.toBigDecimal().times(ratio).toBigInteger(),
                            userStAave
                        )
                    }
                ),
                farmType = ContractType.STAKING,
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
        return Protocol.AAVE_V3
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}