package io.defitrack.protocol.adamant.staking

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.adamant.AdamantService
import io.defitrack.protocol.adamant.AdamantVaultContract
import io.defitrack.protocol.adamant.claimable.AdamantVaultClaimPreparer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
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
                        create(
                            name = "${token.name} vault",
                            identifier = vault.address,
                            stakedToken = token.toFungibleToken(),
                            rewardTokens = listOf(
                                addy.await().toFungibleToken()
                            ),
                            vaultType = "adamant-generic-vault",
                            balanceFetcher = PositionFetcher(
                                vault.address,
                                { user -> balanceOfFunction(user) }
                            ),
                            farmType = ContractType.VAULT,
                            claimableRewardFetcher = ClaimableRewardFetcher(
                                address = vault.address,
                                function = { user ->
                                    vault.getPendingRewardFunction(user)
                                },
                                preparedTransaction = {
                                    AdamantVaultClaimPreparer(
                                        vault
                                    ).prepare()
                                }
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