package io.defitrack.protocol.adamant.staking

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.adamant.AdamantService
import io.defitrack.protocol.adamant.AdamantVaultContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.ADAMANT)
class AdamantVaultMarketProvider(
    private val adamantService: AdamantService,
) : FarmingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.ADAMANT
    }

    val addy = lazyAsync {
        getToken("0xc3fdbadc7c795ef1d6ba111e06ff8f16a20ea539")
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> =
        coroutineScope {
            adamantService.adamantGenericVaults().map {
                AdamantVaultContract(
                    getBlockchainGateway(),
                    it.vaultAddress
                )
            }.map { vault ->
                async {
                    try {
                        val token = getToken(vault.token())
                        val rewardToken = addy.await().toFungibleToken()
                        create(
                            name = "${token.name} vault",
                            identifier = vault.address,
                            stakedToken = token.toFungibleToken(),
                            rewardTokens = listOf(
                                rewardToken
                            ),
                            balanceFetcher = PositionFetcher(
                                vault.address,
                                { user -> balanceOfFunction(user) }
                            ),
                            claimableRewardFetcher = ClaimableRewardFetcher(
                                Reward(
                                    token = rewardToken,
                                    contractAddress = vault.address,
                                    getRewardFunction = vault::getPendingRewardFunction
                                ),
                                preparedTransaction = selfExecutingTransaction(vault::getClaimFunction)
                            )
                        )
                    } catch (ex: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}